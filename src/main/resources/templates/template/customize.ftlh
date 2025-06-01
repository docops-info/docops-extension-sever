<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Customize Library Documentation Template</title>
    <link rel="shortcut icon" href="../images/favicon.svg" type="image/x-icon"/>
    <link href="../css/output.css" rel="stylesheet">
    <script src="../js/htmx.min.2.0.4.js"></script>
    <style>
        .form-section {
            margin-bottom: 2rem;
            padding: 1rem;
            border: 1px solid #e5e7eb;
            border-radius: 0.5rem;
            background-color: #f9fafb;
        }
        .form-section h3 {
            margin-top: 0;
            color: #1f2937;
        }
        .form-group {
            margin-bottom: 1rem;
        }
        label {
            display: block;
            margin-bottom: 0.5rem;
            font-weight: 500;
            color: #374151;
        }
        input[type="text"], textarea {
            width: 100%;
            padding: 0.5rem;
            border: 1px solid #d1d5db;
            border-radius: 0.375rem;
            font-size: 0.875rem;
        }
        textarea {
            min-height: 100px;
        }
        .btn {
            display: inline-block;
            padding: 0.5rem 1rem;
            font-weight: 500;
            text-align: center;
            border-radius: 0.375rem;
            cursor: pointer;
        }
        .btn-primary {
            background-color: #3b82f6;
            color: white;
            border: none;
        }
        .btn-primary:hover {
            background-color: #2563eb;
        }
        .btn-secondary {
            background-color: #9ca3af;
            color: white;
            border: none;
        }
        .btn-secondary:hover {
            background-color: #6b7280;
        }
        .preview-container {
            margin-top: 2rem;
            padding: 1rem;
            border: 1px solid #e5e7eb;
            border-radius: 0.5rem;
            background-color: #f9fafb;
        }
        .preview-iframe {
            width: 100%;
            height: 600px;
            border: 1px solid #d1d5db;
            border-radius: 0.375rem;
        }
    </style>
</head>
<body class="bg-gray-50 text-gray-900">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="flex justify-between items-center mb-8">
            <div>
                <h1 class="text-3xl font-bold text-gray-900">Customize Library Documentation Template</h1>
                <p class="text-gray-600">Fill in the form below to customize your library documentation template.</p>
            </div>
            <div>
                <a href="download" class="btn btn-secondary mr-2">Download Template</a>
            </div>
        </div>

        <form id="templateForm" action="download" method="post">
            <div class="form-section">
                <h3 class="text-xl font-semibold mb-4">Basic Information</h3>
                
                <div class="form-group">
                    <label for="title">Page Title</label>
                    <input type="text" id="title" name="title" value="${title}" required>
                </div>
                
                <div class="form-group">
                    <label for="favicon">Favicon URL</label>
                    <input type="text" id="favicon" name="favicon" value="${favicon}">
                </div>
            </div>

            <div class="form-section">
                <h3 class="text-xl font-semibold mb-4">Header</h3>
                
                <div class="form-group">
                    <label for="logoUrl">Logo URL</label>
                    <input type="text" id="logoUrl" name="logoUrl" value="${logoUrl}">
                </div>
                
                <div class="form-group">
                    <label for="logoAlt">Logo Alt Text</label>
                    <input type="text" id="logoAlt" name="logoAlt" value="${logoAlt}">
                </div>
                
                <div class="form-group">
                    <label for="headerTitle">Header Title</label>
                    <input type="text" id="headerTitle" name="headerTitle" value="${headerTitle}" required>
                </div>
                
                <div class="form-group">
                    <label for="headerDescription">Header Description</label>
                    <input type="text" id="headerDescription" name="headerDescription" value="${headerDescription}">
                </div>
                
                <div class="form-group">
                    <label for="getStartedButtonText">Get Started Button Text</label>
                    <input type="text" id="getStartedButtonText" name="getStartedButtonText" value="${getStartedButtonText}">
                </div>
                
                <div class="form-group">
                    <label for="githubUrl">GitHub URL</label>
                    <input type="text" id="githubUrl" name="githubUrl" value="${githubUrl}">
                </div>
                
                <div class="form-group">
                    <label for="githubButtonText">GitHub Button Text</label>
                    <input type="text" id="githubButtonText" name="githubButtonText" value="${githubButtonText}">
                </div>
            </div>

            <div class="form-section">
                <h3 class="text-xl font-semibold mb-4">Hero Section</h3>
                
                <div class="form-group">
                    <label for="heroTitle">Hero Title</label>
                    <input type="text" id="heroTitle" name="heroTitle" value="${heroTitle}" required>
                </div>
                
                <div class="form-group">
                    <label for="heroDescription">Hero Description</label>
                    <textarea id="heroDescription" name="heroDescription" required>${heroDescription}</textarea>
                </div>
            </div>

            <div class="form-section">
                <h3 class="text-xl font-semibold mb-4">Get Started Section</h3>
                
                <div class="form-group">
                    <label for="getStartedTitle">Get Started Title</label>
                    <input type="text" id="getStartedTitle" name="getStartedTitle" value="${getStartedTitle}">
                </div>
                
                <div class="form-group">
                    <label for="getStartedDescription">Get Started Description</label>
                    <textarea id="getStartedDescription" name="getStartedDescription">${getStartedDescription}</textarea>
                </div>
                
                <div class="form-group">
                    <label for="mavenTitle">Maven Title</label>
                    <input type="text" id="mavenTitle" name="mavenTitle" value="${mavenTitle}">
                </div>
                
                <div class="form-group">
                    <label for="mavenCode">Maven Code</label>
                    <textarea id="mavenCode" name="mavenCode">${mavenCode}</textarea>
                </div>
                
                <div class="form-group">
                    <label for="gradleTitle">Gradle Title</label>
                    <input type="text" id="gradleTitle" name="gradleTitle" value="${gradleTitle}">
                </div>
                
                <div class="form-group">
                    <label for="gradleCode">Gradle Code</label>
                    <textarea id="gradleCode" name="gradleCode">${gradleCode}</textarea>
                </div>
                
                <div class="form-group">
                    <label for="usageTitle">Usage Title</label>
                    <input type="text" id="usageTitle" name="usageTitle" value="${usageTitle}">
                </div>
                
                <div class="form-group">
                    <label for="usageDescription">Usage Description</label>
                    <textarea id="usageDescription" name="usageDescription">${usageDescription}</textarea>
                </div>
                
                <div class="form-group">
                    <label for="usageCode">Usage Code</label>
                    <textarea id="usageCode" name="usageCode">${usageCode}</textarea>
                </div>
                
                <div class="form-group">
                    <label for="usageFooter">Usage Footer</label>
                    <textarea id="usageFooter" name="usageFooter">${usageFooter}</textarea>
                </div>
            </div>

            <div class="form-section">
                <h3 class="text-xl font-semibold mb-4">Features Section</h3>
                
                <div class="form-group">
                    <label for="featuresTitle">Features Title</label>
                    <input type="text" id="featuresTitle" name="featuresTitle" value="${featuresTitle}">
                </div>
                
                <p class="text-sm text-gray-600 mb-4">Note: Feature items are defined in the template. You'll need to customize them directly in the HTML after downloading.</p>
            </div>

            <div class="form-section">
                <h3 class="text-xl font-semibold mb-4">Footer</h3>
                
                <div class="form-group">
                    <label for="footerTitle">Footer Title</label>
                    <input type="text" id="footerTitle" name="footerTitle" value="${footerTitle}">
                </div>
                
                <div class="form-group">
                    <label for="footerDescription">Footer Description</label>
                    <textarea id="footerDescription" name="footerDescription">${footerDescription}</textarea>
                </div>
                
                <div class="form-group">
                    <label for="footerLogoUrl">Footer Logo URL</label>
                    <input type="text" id="footerLogoUrl" name="footerLogoUrl" value="${footerLogoUrl}">
                </div>
                
                <div class="form-group">
                    <label for="footerLogoAlt">Footer Logo Alt Text</label>
                    <input type="text" id="footerLogoAlt" name="footerLogoAlt" value="${footerLogoAlt}">
                </div>
                
                <div class="form-group">
                    <label for="footerCol1Title">Footer Column 1 Title</label>
                    <input type="text" id="footerCol1Title" name="footerCol1Title" value="${footerCol1Title}">
                </div>
                
                <div class="form-group">
                    <label for="footerCol1Links">Footer Column 1 Links (HTML)</label>
                    <textarea id="footerCol1Links" name="footerCol1Links">${footerCol1Links}</textarea>
                </div>
                
                <div class="form-group">
                    <label for="footerCol2Title">Footer Column 2 Title</label>
                    <input type="text" id="footerCol2Title" name="footerCol2Title" value="${footerCol2Title}">
                </div>
                
                <div class="form-group">
                    <label for="footerCol2Links">Footer Column 2 Links (HTML)</label>
                    <textarea id="footerCol2Links" name="footerCol2Links">${footerCol2Links}</textarea>
                </div>
                
                <div class="form-group">
                    <label for="footerCol3Title">Footer Column 3 Title</label>
                    <input type="text" id="footerCol3Title" name="footerCol3Title" value="${footerCol3Title}">
                </div>
                
                <div class="form-group">
                    <label for="footerCol3Links">Footer Column 3 Links (HTML)</label>
                    <textarea id="footerCol3Links" name="footerCol3Links">${footerCol3Links}</textarea>
                </div>
                
                <div class="form-group">
                    <label for="copyright">Copyright Text</label>
                    <input type="text" id="copyright" name="copyright" value="${copyright}">
                </div>
            </div>

            <div class="flex justify-between mt-8">
                <button type="button" id="previewButton" class="btn btn-secondary">Preview</button>
                <button type="submit" class="btn btn-primary">Download Customized Template</button>
            </div>
        </form>

        <div id="previewContainer" class="preview-container" style="display: none;">
            <h3 class="text-xl font-semibold mb-4">Preview</h3>
            <iframe id="previewFrame" class="preview-iframe"></iframe>
        </div>
    </div>

    <script>
        document.getElementById('previewButton').addEventListener('click', function() {
            const form = document.getElementById('templateForm');
            const formData = new FormData(form);
            
            // Show the preview container
            document.getElementById('previewContainer').style.display = 'block';
            
            // Create a form to submit to the preview endpoint
            const previewForm = document.createElement('form');
            previewForm.method = 'POST';
            previewForm.action = 'preview';
            previewForm.target = 'previewFrame';
            previewForm.style.display = 'none';
            
            // Add all form fields to the preview form
            for (const [key, value] of formData.entries()) {
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = key;
                input.value = value;
                previewForm.appendChild(input);
            }
            
            // Add the form to the document, submit it, and remove it
            document.body.appendChild(previewForm);
            previewForm.submit();
            document.body.removeChild(previewForm);
            
            // Scroll to the preview
            document.getElementById('previewContainer').scrollIntoView({ behavior: 'smooth' });
        });
    </script>
</body>
</html>