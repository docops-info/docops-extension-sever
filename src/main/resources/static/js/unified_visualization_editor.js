class UnifiedVisualizationEditor {
    constructor() {
        this.currentVisualization = null;
        this.currentConfig = {};
        this.previewContainer = document.getElementById('preview-container');
        this.configPanel = document.getElementById('config-panel');
        this.currentVizTitle = document.getElementById('current-viz-title');
        this.exportSvgBtn = document.getElementById('export-svg');
        this.exportConfigBtn = document.getElementById('export-config');
        
        this.init();
    }

    async init() {
        await this.loadVisualizationTypes();
        this.setupEventHandlers();
    }

    async loadVisualizationTypes() {
        const visualizationTypes = [
            {
                id: 'featurecard',
                name: 'Feature Cards',
                description: 'Interactive cards for showcasing features',
                icon: '🎯',
                endpoint: '../api/featurecard',
                color: 'bg-blue-100 text-blue-800'
            },
            {
                id: 'roadmap',
                name: 'Roadmaps',
                description: 'Timeline-based project planning',
                icon: '🗺️',
                endpoint: '../api/roadmap',
                color: 'bg-green-100 text-green-800'
            },
            {
                id: 'timeline',
                name: 'Timelines',
                description: 'Linear progression visualizations',
                icon: '📅',
                endpoint: '../api/timeline',
                color: 'bg-purple-100 text-purple-800'
            },
            {
                id: 'scorecard',
                name: 'Scorecards',
                description: 'Metrics and KPI dashboards',
                icon: '📊',
                endpoint: '../api/scorecard',
                color: 'bg-orange-100 text-orange-800'
            },
            {
                id: 'chart',
                name: 'Charts',
                description: 'Various chart types (bar, pie, etc.)',
                icon: '📈',
                endpoint: '../api/chart',
                color: 'bg-red-100 text-red-800'
            },
            {
                id: 'swimlane',
                name: 'Swimlanes',
                description: 'Process flow diagrams',
                icon: '🏊',
                endpoint: '../api/swimlane',
                color: 'bg-teal-100 text-teal-800'
            },
            {
                id: 'wordcloud',
                name: 'Word Clouds',
                description: 'Text-based visualizations',
                icon: '☁️',
                endpoint: '../api/wordcloud',
                color: 'bg-indigo-100 text-indigo-800'
            },
            {
                id: 'planner',
                name: 'Planners',
                description: 'Project planning layouts',
                icon: '📋',
                endpoint: '../api/planner',
                color: 'bg-pink-100 text-pink-800'
            }
        ];

        this.renderVisualizationTypes(visualizationTypes);
    }

    renderVisualizationTypes(types) {
        const container = document.getElementById('visualization-types');
        container.innerHTML = '';

        types.forEach(type => {
            const card = document.createElement('div');
            card.className = `visualization-card p-4 rounded-lg border-2 border-gray-200 cursor-pointer hover:border-blue-400 ${type.color}`;
            card.innerHTML = `
                <div class="flex items-center space-x-3">
                    <div class="text-2xl">${type.icon}</div>
                    <div class="flex-1">
                        <h3 class="font-semibold text-gray-800">${type.name}</h3>
                        <p class="text-sm text-gray-600">${type.description}</p>
                    </div>
                </div>
            `;
            
            card.addEventListener('click', () => this.selectVisualization(type));
            container.appendChild(card);
        });
    }

    async selectVisualization(type) {
        this.currentVisualization = type;
        this.currentVizTitle.textContent = type.name;
        
        // Remove active state from all cards
        document.querySelectorAll('.visualization-card').forEach(card => {
            card.classList.remove('border-blue-400', 'bg-blue-50');
        });
        
        // Add active state to selected card
        event.currentTarget.classList.add('border-blue-400', 'bg-blue-50');
        
        await this.loadConfiguration(type);
        this.enableExportButtons();
    }

    async loadConfiguration(type) {
        try {
            // Load configuration form for the selected visualization type
            const response = await fetch(`${type.endpoint}/config`);
            const configHtml = await response.text();
            
            this.configPanel.innerHTML = configHtml;
            this.setupConfigurationHandlers();
        } catch (error) {
            console.error('Failed to load configuration:', error);
            this.configPanel.innerHTML = this.getDefaultConfiguration(type);
            this.setupConfigurationHandlers();
        }
    }

    getDefaultConfiguration(type) {
        // Provide default configuration forms for each type
        switch (type.id) {
            case 'featurecard':
                return this.getFeatureCardConfig();
            case 'roadmap':
                return this.getRoadmapConfig();
            case 'timeline':
                return this.getTimelineConfig();
            case 'scorecard':
                return this.getScorecardConfig();
            case 'chart':
                return this.getChartConfig();
            default:
                return this.getGenericConfig();
        }
    }

    getFeatureCardConfig() {
        return `
            <div class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Theme</label>
                    <select name="theme" class="w-full p-2 border border-gray-300 rounded-md">
                        <option value="light">Light</option>
                        <option value="dark">Dark</option>
                        <option value="auto">Auto</option>
                    </select>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Layout</label>
                    <select name="layout" class="w-full p-2 border border-gray-300 rounded-md">
                        <option value="grid">Grid</option>
                        <option value="row">Row</option>
                        <option value="column">Column</option>
                    </select>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Card Data</label>
                    <textarea name="cardData" rows="10" class="w-full p-2 border border-gray-300 rounded-md" placeholder="Title | Description | Emoji | ColorScheme
Feature 1 | This is a description | 🚀 | BLUE
>> This is a detail about feature 1
Feature 2 | Another description | 🔍 | GREEN"></textarea>
                </div>
            </div>
        `;
    }

    getRoadmapConfig() {
        return `
            <div class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Style</label>
                    <select name="style" class="w-full p-2 border border-gray-300 rounded-md">
                        <option value="horizontal">Horizontal</option>
                        <option value="vertical">Vertical</option>
                        <option value="timeline">Timeline</option>
                    </select>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Theme</label>
                    <select name="theme" class="w-full p-2 border border-gray-300 rounded-md">
                        <option value="light">Light</option>
                        <option value="dark">Dark</option>
                        <option value="glass">Glass</option>
                    </select>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Roadmap Data</label>
                    <textarea name="roadmapData" rows="10" class="w-full p-2 border border-gray-300 rounded-md" placeholder="Quarter | Item | Status | Priority
Q1 2024 | Feature A | In Progress | High
Q2 2024 | Feature B | Planned | Medium
Q3 2024 | Feature C | Planned | Low"></textarea>
                </div>
            </div>
        `;
    }

    getTimelineConfig() {
        return `
            <div class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Orientation</label>
                    <select name="orientation" class="w-full p-2 border border-gray-300 rounded-md">
                        <option value="horizontal">Horizontal</option>
                        <option value="vertical">Vertical</option>
                    </select>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Style</label>
                    <select name="style" class="w-full p-2 border border-gray-300 rounded-md">
                        <option value="normal">Normal</option>
                        <option value="modern">Modern</option>
                        <option value="minimal">Minimal</option>
                    </select>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Timeline Data</label>
                    <textarea name="timelineData" rows="10" class="w-full p-2 border border-gray-300 rounded-md" placeholder="Date | Event | Description
2024-01-15 | Project Start | Project kickoff meeting
2024-02-01 | Milestone 1 | First milestone completed
2024-03-15 | Milestone 2 | Second milestone completed"></textarea>
                </div>
            </div>
        `;
    }

    getScorecardConfig() {
        return `
            <div class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Theme</label>
                    <select name="theme" class="w-full p-2 border border-gray-300 rounded-md">
                        <option value="light">Light</option>
                        <option value="dark">Dark</option>
                        <option value="colorful">Colorful</option>
                    </select>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Layout</label>
                    <select name="layout" class="w-full p-2 border border-gray-300 rounded-md">
                        <option value="grid">Grid</option>
                        <option value="list">List</option>
                        <option value="compact">Compact</option>
                    </select>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Scorecard Data</label>
                    <textarea name="scorecardData" rows="10" class="w-full p-2 border border-gray-300 rounded-md" placeholder="Metric | Value | Target | Status
Sales | 85% | 90% | Warning
Quality | 95% | 90% | Success
Performance | 78% | 80% | Danger"></textarea>
                </div>
            </div>
        `;
    }

    getChartConfig() {
        return `
            <div class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Chart Type</label>
                    <select name="chartType" class="w-full p-2 border border-gray-300 rounded-md">
                        <option value="bar">Bar Chart</option>
                        <option value="pie">Pie Chart</option>
                        <option value="line">Line Chart</option>
                        <option value="combination">Combination Chart</option>
                    </select>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Theme</label>
                    <select name="theme" class="w-full p-2 border border-gray-300 rounded-md">
                        <option value="light">Light</option>
                        <option value="dark">Dark</option>
                        <option value="glass">Glass</option>
                    </select>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Chart Data</label>
                    <textarea name="chartData" rows="10" class="w-full p-2 border border-gray-300 rounded-md" placeholder="Category | Value | Color
Sales | 120 | #3B82F6
Marketing | 80 | #10B981
Support | 95 | #F59E0B
Development | 110 | #EF4444"></textarea>
                </div>
            </div>
        `;
    }

    getGenericConfig() {
        return `
            <div class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Configuration</label>
                    <textarea name="config" rows="10" class="w-full p-2 border border-gray-300 rounded-md" placeholder="Enter configuration data here..."></textarea>
                </div>
            </div>
        `;
    }

    setupConfigurationHandlers() {
        // Add event listeners to all form elements
        const formElements = this.configPanel.querySelectorAll('input, select, textarea');
        formElements.forEach(element => {
            element.addEventListener('input', () => this.updatePreview());
            element.addEventListener('change', () => this.updatePreview());
        });
    }

    async updatePreview() {
        if (!this.currentVisualization) return;

        // Collect configuration data
        const config = this.collectConfiguration();
        
        // Debounce the preview update
        clearTimeout(this.previewTimeout);
        this.previewTimeout = setTimeout(async () => {
            try {
                const response = await fetch(`${this.currentVisualization.endpoint}/render`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(config)
                });

                if (response.ok) {
                    const svg = await response.text();
                    this.previewContainer.innerHTML = svg;
                    this.currentConfig = config;
                } else {
                    throw new Error('Failed to render preview');
                }
            } catch (error) {
                console.error('Preview update failed:', error);
                this.previewContainer.innerHTML = `
                    <div class="text-red-500 text-center">
                        <p>Failed to render preview</p>
                        <p class="text-sm">${error.message}</p>
                    </div>
                `;
            }
        }, 500);
    }

    collectConfiguration() {
        const config = {
            type: this.currentVisualization.id
        };

        const formElements = this.configPanel.querySelectorAll('input, select, textarea');
        formElements.forEach(element => {
            config[element.name] = element.value;
        });

        return config;
    }

    setupEventHandlers() {
        this.exportSvgBtn.addEventListener('click', () => this.exportSvg());
        this.exportConfigBtn.addEventListener('click', () => this.exportConfig());
    }

    enableExportButtons() {
        this.exportSvgBtn.disabled = false;
        this.exportConfigBtn.disabled = false;
    }

    exportSvg() {
        const svgElement = this.previewContainer.querySelector('svg');
        if (!svgElement) {
            alert('No SVG to export. Please generate a preview first.');
            return;
        }

        const svgData = new XMLSerializer().serializeToString(svgElement);
        const blob = new Blob([svgData], { type: 'image/svg+xml' });
        const url = URL.createObjectURL(blob);
        
        const link = document.createElement('a');
        link.href = url;
        link.download = `${this.currentVisualization.id}-${Date.now()}.svg`;
        link.click();
        
        URL.revokeObjectURL(url);
    }

    exportConfig() {
        const config = this.collectConfiguration();
        const configData = JSON.stringify(config, null, 2);
        const blob = new Blob([configData], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        
        const link = document.createElement('a');
        link.href = url;
        link.download = `${this.currentVisualization.id}-config-${Date.now()}.json`;
        link.click();
        
        URL.revokeObjectURL(url);
    }
}

// Initialize the editor when the page loads
document.addEventListener('DOMContentLoaded', () => {
    new UnifiedVisualizationEditor();
});
