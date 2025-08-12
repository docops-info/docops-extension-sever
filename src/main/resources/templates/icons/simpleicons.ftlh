<!-- src/main/resources/templates/icons/simpleicons.ftlh -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SimpleIcons Browser</title>
    <link href="css/output.css" rel="stylesheet">
    <script src="js/htmx.min.2.0.4.js"></script>
    <style>
        .icon-card {
            transition: transform 0.2s, box-shadow 0.2s;
        }
        .icon-card:hover {
            transform: translateY(-3px);
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
        }
        .letter-nav {
            position: sticky;
            top: 0;
            background: white;
            z-index: 10;
            padding: 1rem 0;
            border-bottom: 1px solid #e5e7eb;
        }
        .htmx-indicator {
            opacity: 0;
            transition: opacity 500ms ease-in;
        }
        .htmx-request .htmx-indicator {
            opacity: 1;
        }
        .htmx-request.htmx-indicator {
            opacity: 1;
        }
        .modal {
            transition: opacity 0.3s ease;
        }
        .modal.show {
            opacity: 1;
            pointer-events: all;
        }
        .modal.hide {
            opacity: 0;
            pointer-events: none;
        }
    </style>
</head>
<body class="bg-gray-50 text-gray-900" 
      hx-boost="true" 
      hx-target="#content" 
      hx-push-url="false"
>

    <header class="bg-white shadow-sm border-b">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between items-center py-6">
                <div class="flex items-center space-x-4">
                    <div>
                        <h1 class="text-2xl font-bold text-gray-900">SimpleIcons Browser</h1>
                        <p class="text-sm text-gray-600">Browse SVG icons for popular brands</p>
                    </div>
                </div>
                <div>
                    <form class="flex items-center" hx-get="icons/search" hx-target="#search-results" hx-trigger="submit, input changed delay:500ms">
                        <input 
                            type="text" 
                            name="query" 
                            placeholder="Search icons..." 
                            class="border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            hx-indicator="#search-indicator">
                        <div id="search-indicator" class="htmx-indicator ml-2">
                            <svg class="animate-spin h-5 w-5 text-blue-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                            </svg>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </header>

    <main id="content" class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <!-- Search Results Section -->
        <div id="search-results"></div>

        <!-- Alphabet Navigation -->
        <div class="letter-nav flex flex-wrap justify-center gap-2 mb-8">
            <#list letters as letter>
                <a href="icons#${letter}"
                   class="letter-link px-3 py-1 rounded-md shadow-sm font-medium <#if letter == currentLetter>bg-blue-500 text-white<#else>bg-white text-gray-800 hover:bg-gray-100</#if>"
                   hx-get="icons/letter/${letter}"
                   hx-target="#icon-grid-container"
                   hx-push-url="false">
                    ${letter}
                </a>
            </#list>
        </div>
        
        <!-- Icon Grid Container -->
        <div id="icon-grid-container">
            <#include "icon-grid.ftlh">
        </div>
    </main>

    <!-- Modal for icon details -->
    <div id="icon-details-modal" class="modal hide fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg shadow-xl max-w-md w-full p-6 m-4">
            <div id="icon-details-content">
                <!-- Content loaded via HTMX -->
            </div>
            <div class="mt-4 flex justify-end">
                <button class="bg-gray-200 hover:bg-gray-300 text-gray-800 px-4 py-2 rounded-md" onclick="closeModal()">
                    Close
                </button>
            </div>
        </div>
    </div>

    <script>
        function showModal() {
            const modal = document.getElementById('icon-details-modal');
            modal.classList.remove('hide');
            modal.classList.add('show');
        }

        function closeModal() {
            const modal = document.getElementById('icon-details-modal');
            modal.classList.remove('show');
            modal.classList.add('hide');
        }

        // Handle clicks on show-icon-details buttons
        document.body.addEventListener('click', function(e) {
            if (e.target.closest('.show-icon-details')) {
                e.preventDefault();
                showModal();
            }
        });

        // Close modal when clicking outside
        document.getElementById('icon-details-modal').addEventListener('click', function(e) {
            if (e.target === this) {
                closeModal();
            }
        });

        // Update navigation after HTMX loads new content
        document.addEventListener('htmx:afterSwap', function(e) {
            if (e.detail.target.id === 'icon-grid-container') {
                // Update active letter styling
                const currentLetter = document.querySelector('#current-letter')?.textContent;
                if (currentLetter) {
                    document.querySelectorAll('.letter-link').forEach(link => {
                        if (link.textContent.trim() === currentLetter) {
                            link.className = 'letter-link px-3 py-1 rounded-md shadow-sm font-medium bg-blue-500 text-white';
                        } else {
                            link.className = 'letter-link px-3 py-1 rounded-md shadow-sm font-medium bg-white text-gray-800 hover:bg-gray-100';
                        }
                    });
                }
            }
        });

        // Copy to clipboard function
        function copyToClipboard(text) {
            if (navigator.clipboard) {
                navigator.clipboard.writeText(text).then(() => {
                    alert('SVG copied to clipboard!');
                }).catch(err => {
                    console.error('Could not copy text: ', err);
                });
            } else {
                // Fallback for older browsers
                const textArea = document.createElement('textarea');
                textArea.value = text;
                document.body.appendChild(textArea);
                textArea.select();
                try {
                    document.execCommand('copy');
                    alert('SVG copied to clipboard!');
                } catch (err) {
                    console.error('Could not copy text: ', err);
                }
                document.body.removeChild(textArea);
            }
        }
    </script>
</body>
</html>
