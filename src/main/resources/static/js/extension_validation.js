// validator.js

class ExtensionValidator {
  constructor(capability) {
    this.capability = capability;
    this.rules = this.getValidationRules(capability);
  }

  getValidationRules(capability) {
    const rules = {
      bar: {
        metadata: {
          title: { required: false, type: 'string' },
          yLabel: { required: false, type: 'string' },
          xLabel: { required: false, type: 'string' },
          baseColor: { required: false, type: 'color' },
          type: { required: false, type: 'string', values: ['R', 'H'] }
        },
        rows: {
          minColumns: 2,
          maxColumns: 2,
          columnTypes: ['string', 'number']
        }
      },
      line: {
        metadata: {
          title: { required: false, type: 'string' },
          width: { required: false, type: 'number', min: 100, max: 2000 },
          smooth: { required: false, type: 'boolean' },
          darkMode: { required: false, type: 'boolean' }
        },
        rows: {
          minColumns: 3,
          maxColumns: 3,
          columnTypes: ['string', 'string', 'number']
        }
      },
      badge: {
        rows: {
          minColumns: 6,
          maxColumns: 7,
          columnTypes: ['string', 'string', 'string', 'color', 'color', 'string', 'color']
        }
      },
      timeline: {
        type: 'yaml',
        requiredFields: ['date', 'text']
      },
      buttons: {
        type: 'json',
        schema: {
          buttons: { required: true, type: 'array' },
          buttonType: { required: false, type: 'string', values: ['HEX', 'RECT', 'CIRCLE'] },
          theme: { required: false, type: 'object' }
        }
      }
    };

    return rules[capability] || {};
  }

  validate(parsedData, content) {
    const errors = [];
    const warnings = [];

    if (!parsedData) {
      return { valid: false, errors: [{ message: 'Failed to parse content' }], warnings };
    }

    // Validate based on capability
    switch(this.rules.type) {
      case 'json':
        this.validateJSON(parsedData, errors, warnings);
        break;
      case 'yaml':
        this.validateYAML(parsedData, errors, warnings);
        break;
      default:
        this.validateTabular(parsedData, errors, warnings);
    }

    return {
      valid: errors.length === 0,
      errors,
      warnings
    };
  }

  validateJSON(data, errors, warnings) {
    if (this.rules.schema) {
      for (const [field, rule] of Object.entries(this.rules.schema)) {
        if (rule.required && !(field in data)) {
          errors.push({ field, message: `Required field '${field}' is missing` });
        }

        if (field in data) {
          const value = data[field];
          
          if (rule.type === 'array' && !Array.isArray(value)) {
            errors.push({ field, message: `Field '${field}' must be an array` });
          }
          
          if (rule.type === 'object' && typeof value !== 'object') {
            errors.push({ field, message: `Field '${field}' must be an object` });
          }
          
          if (rule.values && !rule.values.includes(value)) {
            warnings.push({ field, message: `Field '${field}' has unexpected value: ${value}` });
          }
        }
      }
    }
  }

  validateYAML(data, errors, warnings) {
    if (this.rules.requiredFields) {
      const items = Array.isArray(data) ? data : [data];
      
      items.forEach((item, index) => {
        this.rules.requiredFields.forEach(field => {
          if (!(field in item)) {
            errors.push({ 
              index, 
              field, 
              message: `Item ${index + 1} is missing required field '${field}'` 
            });
          }
        });
      });
    }
  }

  validateTabular(data, errors, warnings) {
    // Validate metadata
    if (this.rules.metadata && data.metadata) {
      for (const [key, rule] of Object.entries(this.rules.metadata)) {
        const value = data.metadata[key];
        
        if (rule.required && !value) {
          errors.push({ field: key, message: `Required metadata '${key}' is missing` });
          continue;
        }

        if (value) {
          if (rule.type === 'number' && isNaN(value)) {
            errors.push({ field: key, message: `'${key}' must be a number` });
          }
          
          if (rule.type === 'color' && !this.isValidColor(value)) {
            errors.push({ field: key, message: `'${key}' must be a valid color` });
          }
          
          if (rule.type === 'boolean' && !['true', 'false'].includes(value.toLowerCase())) {
            errors.push({ field: key, message: `'${key}' must be true or false` });
          }
          
          if (rule.values && !rule.values.includes(value)) {
            warnings.push({ field: key, message: `'${key}' has unexpected value: ${value}` });
          }
          
          if (rule.min !== undefined && parseFloat(value) < rule.min) {
            errors.push({ field: key, message: `'${key}' must be at least ${rule.min}` });
          }
          
          if (rule.max !== undefined && parseFloat(value) > rule.max) {
            errors.push({ field: key, message: `'${key}' must be at most ${rule.max}` });
          }
        }
      }
    }

    // Validate rows
    if (this.rules.rows && data.rows) {
      data.rows.forEach((row, index) => {
        if (row.length < this.rules.rows.minColumns) {
          errors.push({ 
            line: index + 1, 
            message: `Row ${index + 1} has too few columns (${row.length}). Expected at least ${this.rules.rows.minColumns}` 
          });
        }
        
        if (row.length > this.rules.rows.maxColumns) {
          errors.push({ 
            line: index + 1, 
            message: `Row ${index + 1} has too many columns (${row.length}). Expected at most ${this.rules.rows.maxColumns}` 
          });
        }

        // Validate column types
        if (this.rules.rows.columnTypes) {
          row.forEach((cell, colIndex) => {
            const expectedType = this.rules.rows.columnTypes[colIndex];
            if (expectedType && !this.validateType(cell, expectedType)) {
              errors.push({
                line: index + 1,
                column: colIndex + 1,
                message: `Column ${colIndex + 1} in row ${index + 1} should be ${expectedType}, got: ${cell}`
              });
            }
          });
        }
      });
    }
  }

  validateType(value, type) {
    switch(type) {
      case 'number':
        return !isNaN(value) && value.trim() !== '';
      case 'color':
        return this.isValidColor(value);
      case 'string':
        return typeof value === 'string';
      case 'boolean':
        return ['true', 'false', 'yes', 'no'].includes(value.toLowerCase());
      default:
        return true;
    }
  }

  isValidColor(color) {
    return /^#([0-9A-F]{3}){1,2}$/i.test(color) || 
           /^rgb\(\s*\d+\s*,\s*\d+\s*,\s*\d+\s*\)$/i.test(color) ||
           /^rgba\(\s*\d+\s*,\s*\d+\s*,\s*\d+\s*,\s*[\d.]+\s*\)$/i.test(color);
  }
}
