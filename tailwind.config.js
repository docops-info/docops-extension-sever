/** @type {import('tailwindcss').Config} */
module.exports = {
    darkMode: 'class',
    content: [
        "src/main/resources/**/*.{html,js,ftlh}",
        "./src/**/*.html",  // Add this to ensure all HTML files are scanned
        "./**/*.html"       // Broader scan if needed
    ],
    theme: {
        extend: {
            colors: {
                // Custom colors seen in your layout
                blue: {
                    500: '#3B82F6',
                    600: '#2563EB',
                    700: '#1D4ED8',
                },
                purple: {
                    500: '#8B5CF6',
                    600: '#7C3AED',
                },
                green: {
                    100: '#D1FAE5',
                    300: '#6EE7B7',
                    500: '#10B981',
                    600: '#059669',
                },
                gray: {
                    50: '#F9FAFB',
                    100: '#F3F4F6',
                    300: '#D1D5DB',
                    400: '#9CA3AF',
                    500: '#6B7280',
                    600: '#4B5563',
                    700: '#374151',
                    800: '#1F2937',
                    900: '#111827',
                },
                red: {
                    100: '#FEE2E2',
                    500: '#EF4444',
                    600: '#DC2626',
                },
                yellow: {
                    500: '#F59E0B',
                },
                orange: {
                    100: '#FFEDD5',
                    600: '#EA580C',
                },
                indigo: {
                    100: '#E0E7FF',
                    600: '#4F46E5',
                },
            },
            gradientColorStops: {
                // Gradient configurations used in the template
                'blue-600': '#2563EB',
                'purple-600': '#7C3AED',
            },
            animation: {
                'spin': 'spin 1s linear infinite',
                'pulse': 'pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite',
            },
            keyframes: {
                spin: {
                    to: { transform: 'rotate(360deg)' },
                },
                pulse: {
                    '0%, 100%': { opacity: 1 },
                    '50%': { opacity: .5 },
                },
            },
        },
    },
    plugins: [],
}
