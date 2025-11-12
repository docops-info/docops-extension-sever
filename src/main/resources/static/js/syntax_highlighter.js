// highlighter.js

class SyntaxHighlighter {
  constructor(textarea, outputElement) {
    this.textarea = textarea;
    this.outputElement = outputElement;
    this.capability = null;
  }

  setCapability(capability) {
    this.capability = capability;
    this.highlight();
  }

  highlight() {
    const content = this.textarea.value;
    if (!content) {
      this.outputElement.innerHTML = '';
      return;
    }

    const parser = new ExtensionParser(this.capability);
    const format = parser.detectFormat(content);
    
    let highlighted = '';
    
    switch(format) {
      case 'json':
        highlighted = this.highlightJSON(content);
        break;
      case 'tabular':
        highlighted = this.highlightTabular(content);
        break;
      case 'nameValue':
        highlighted = this.highlightNameValue(content);
        break;
      case 'yaml':
        highlighted = this.highlightYAML(content);
        break;
      default:
        highlighted = this.escapeHtml(content);
    }

    this.outputElement.innerHTML = highlighted;
  }

  highlightJSON(content) {
    try {
      const parsed = JSON.parse(content);
      const formatted = JSON.stringify(parsed, null, 2);
      
      return formatted
        .replace(/("(?:\\.|[^"\\])*")\s*:/g, '<span class="json-key">$1</span>:')
        .replace(/:\s*("(?:\\.|[^"\\])*")/g, ': <span class="json-string">$1</span>')
        .replace(/:\s*(\d+\.?\d*)/g, ': <span class="json-number">$1</span>')
        .replace(/:\s*(true|false)/g, ': <span class="json-boolean">$1</span>')
        .replace(/:\s*(null)/g, ': <span class="json-null">$1</span>');
    } catch (e) {
      return `<span class="error">${this.escapeHtml(content)}</span>`;
    }
  }

  highlightTabular(content) {
    const lines = content.split('\n');
    const highlighted = lines.map((line, index) => {
      const trimmed = line.trim();
      
      if (trimmed === '----' || trimmed === '---') {
        return `<span class="delimiter">${this.escapeHtml(line)}</span>`;
      }
      
      if (trimmed.match(/^(\w+)\s*=\s*(.+)$/)) {
        return line.replace(/^(\s*)(\w+)(\s*=\s*)(.+)$/, 
          '$1<span class="metadata-key">$2</span>$3<span class="metadata-value">$4</span>');
      }
      
      if (trimmed.includes('|')) {
        const parts = line.split('|');
        const highlighted = parts.map((part, i) => 
          `<span class="cell cell-${i % 2 === 0 ? 'even' : 'odd'}">${this.escapeHtml(part)}</span>`
        ).join('<span class="pipe">|</span>');
        return highlighted;
      }
      
      return this.escapeHtml(line);
    });

    return highlighted.join('\n');
  }

  highlightNameValue(content) {
    const lines = content.split('\n');
    return lines.map(line => {
      if (line.trim().startsWith('#')) {
        return `<span class="comment">${this.escapeHtml(line)}</span>`;
      }
      
      return line.replace(/^(\s*)(\w+)(\s*=\s*)(.+)$/, 
        '$1<span class="key">$2</span>$3<span class="value">$4</span>');
    }).join('\n');
  }

  highlightYAML(content) {
    const lines = content.split('\n');
    return lines.map(line => {
      if (line.trim().startsWith('#')) {
        return `<span class="comment">${this.escapeHtml(line)}</span>`;
      }
      
      if (line.trim().startsWith('-')) {
        return line.replace(/^(\s*)(-)(.+)$/, 
          '$1<span class="list-marker">$2</span><span class="list-item">$3</span>');
      }
      
      return line.replace(/^(\s*)(\w+)(\s*:\s*)(.*)$/, 
        '$1<span class="yaml-key">$2</span>$3<span class="yaml-value">$4</span>');
    }).join('\n');
  }

  escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }
}
