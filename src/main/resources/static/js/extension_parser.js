// parser.js

class ExtensionParser {
  constructor(capability) {
    this.capability = capability;
    this.errors = [];
    this.warnings = [];
  }

  parse(content) {
    this.errors = [];
    this.warnings = [];

    const format = this.detectFormat(content);
    
    switch(format) {
      case 'json':
        return this.parseJSON(content);
      case 'tabular':
        return this.parseTabular(content);
      case 'nameValue':
        return this.parseNameValue(content);
      case 'yaml':
        return this.parseYAML(content);
      default:
        this.errors.push({ line: 0, message: 'Unknown format' });
        return null;
    }
  }

  detectFormat(content) {
    const trimmed = content.trim();
    
    if (trimmed.startsWith('{') && trimmed.endsWith('}')) {
      return 'json';
    }
    
    if (trimmed.match(/^[\w]+:/m)) {
      return 'yaml';
    }
    
    if (trimmed.includes('|') || trimmed.includes('---')) {
      return 'tabular';
    }
    
    if (trimmed.match(/^\w+=/m)) {
      return 'nameValue';
    }
    
    return 'unknown';
  }

  parseJSON(content) {
    try {
      return JSON.parse(content);
    } catch (e) {
      this.errors.push({
        line: this.getErrorLine(content, e),
        column: e.columnNumber || 0,
        message: e.message,
        type: 'syntax'
      });
      return null;
    }
  }

  parseTabular(content) {
    const lines = content.split('\n');
    const result = {
      headers: [],
      metadata: {},
      rows: []
    };

    let inMetadata = false;
    let inData = false;
    let headerLine = null;

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim();
      
      if (!line) continue;

      // Detect section markers
      if (line === '----' || line === '---') {
        if (!inMetadata) {
          inMetadata = true;
        } else {
          inMetadata = false;
          inData = true;
        }
        continue;
      }

      if (inMetadata) {
        // Parse metadata (name=value)
        const match = line.match(/^(\w+)\s*=\s*(.+)$/);
        if (match) {
          result.metadata[match[1]] = match[2];
        } else {
          this.warnings.push({
            line: i + 1,
            message: `Invalid metadata format: ${line}`
          });
        }
      } else if (inData) {
        // Parse tabular data
        if (!headerLine && line.includes('|')) {
          headerLine = i;
          result.headers = line.split('|').map(h => h.trim());
        } else if (line.includes('|')) {
          const values = line.split('|').map(v => v.trim());
          if (values.length !== result.headers.length && result.headers.length > 0) {
            this.errors.push({
              line: i + 1,
              message: `Column count mismatch. Expected ${result.headers.length}, got ${values.length}`
            });
          }
          result.rows.push(values);
        }
      }
    }

    return result;
  }

  parseNameValue(content) {
    const lines = content.split('\n');
    const result = {};

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim();
      if (!line || line.startsWith('#')) continue;

      const match = line.match(/^(\w+)\s*=\s*(.+)$/);
      if (match) {
        const [, key, value] = match;
        result[key] = this.parseValue(value);
      } else {
        this.errors.push({
          line: i + 1,
          message: `Invalid name=value format: ${line}`
        });
      }
    }

    return result;
  }

  parseYAML(content) {
    // Simple YAML parser for basic cases
    const lines = content.split('\n');
    const result = {};
    let currentArray = null;
    let currentKey = null;

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i];
      const trimmed = line.trim();
      
      if (!trimmed || trimmed.startsWith('#')) continue;

      if (trimmed.startsWith('-')) {
        const value = trimmed.substring(1).trim();
        if (currentArray) {
          currentArray.push(value);
        }
      } else {
        const match = line.match(/^(\s*)(\w+):\s*(.*)$/);
        if (match) {
          const [, indent, key, value] = match;
          currentKey = key;
          
          if (value) {
            result[key] = value;
            currentArray = null;
          } else {
            result[key] = [];
            currentArray = result[key];
          }
        }
      }
    }

    return result;
  }

  parseValue(value) {
    // Try to parse as number
    if (!isNaN(value) && value.trim() !== '') {
      return parseFloat(value);
    }
    
    // Try to parse as boolean
    if (value.toLowerCase() === 'true') return true;
    if (value.toLowerCase() === 'false') return false;
    
    // Return as string
    return value;
  }

  getErrorLine(content, error) {
    // Extract line number from error message if available
    const match = error.message.match(/line (\d+)/i);
    return match ? parseInt(match[1]) : 0;
  }

  hasErrors() {
    return this.errors.length > 0;
  }

  getErrors() {
    return this.errors;
  }

  getWarnings() {
    return this.warnings;
  }
}
