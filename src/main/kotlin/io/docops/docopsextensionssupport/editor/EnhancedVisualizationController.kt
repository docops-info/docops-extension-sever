package io.docops.docopsextensionssupport.editor

import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.http.MediaType

@RestController
@RequestMapping("/api")
class EnhancedVisualizationController {

    @GetMapping("/featurecard/config")
    fun getFeatureCardConfig(): ResponseEntity<String> {
        val configHtml = """
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
                <div>
                    <button type="button" class="w-full bg-blue-600 text-white p-2 rounded-md hover:bg-blue-700" onclick="loadTemplate('featurecard-basic')">Load Basic Template</button>
                </div>
            </div>
        """
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(configHtml)
    }

    @GetMapping("/roadmap/config")
    fun getRoadmapConfig(): ResponseEntity<String> {
        val configHtml = """
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
Q2 2024 | Feature B | Planned | Medium"></textarea>
                </div>
            </div>
        """
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(configHtml)
    }

    // Add similar methods for other visualization types...
}
