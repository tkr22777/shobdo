/**
 * Theme Management for Shobdo
 * Centralizes all theme-related functionality
 */

// Available themes
const THEMES = ['green', 'dark', 'blue', 'light'];

/**
 * Initialize theme from localStorage or default
 * @returns {string} The current theme
 */
function initTheme() {
    const currentTheme = localStorage.getItem('theme') || 'green';
    applyTheme(currentTheme);
    return currentTheme;
}

/**
 * Apply a theme to the document
 * @param {string} theme - Theme name to apply
 */
function applyTheme(theme) {
    // Apply theme attribute to document
    if (theme === 'green') {
        document.documentElement.removeAttribute('data-theme');
    } else {
        document.documentElement.setAttribute('data-theme', theme);
    }
    
    // Dispatch theme change event
    document.dispatchEvent(new CustomEvent('themechange', { 
        detail: { theme } 
    }));
}

/**
 * Toggle to the next theme in sequence
 */
function cycleTheme() {
    const currentTheme = localStorage.getItem('theme') || 'green';
    const currentIndex = THEMES.indexOf(currentTheme);
    const nextIndex = (currentIndex + 1) % THEMES.length;
    const newTheme = THEMES[nextIndex];
    
    // Apply and save new theme
    applyTheme(newTheme);
    localStorage.setItem('theme', newTheme);
    
    // Update any theme selectors to show the new theme
    updateThemeSelectors(newTheme);
    
    return newTheme;
}

/**
 * Update all theme selectors to show the current theme
 * @param {string} theme - Current theme name
 */
function updateThemeSelectors(theme) {
    // Find all theme selector dropdowns and update them
    const selectors = document.querySelectorAll('[id="themeSelector"]');
    selectors.forEach(selector => {
        if (selector && selector.tagName === 'SELECT') {
            selector.value = theme;
        }
    });
}

/**
 * Set up theme selector UI element
 * @param {string} selectorId - ID of the select element
 */
function setupThemeSelector(selectorId) {
    const selectors = document.querySelectorAll(`#${selectorId}`);
    
    selectors.forEach(selector => {
        if (!selector || selector.tagName !== 'SELECT') return;
        
        // Set initial value based on current theme
        const currentTheme = localStorage.getItem('theme') || 'green';
        selector.value = currentTheme;
        
        // Remove any existing event listeners to prevent duplicates
        selector.removeEventListener('change', handleThemeChange);
        
        // Add change listener
        selector.addEventListener('change', handleThemeChange);
    });
}

/**
 * Handle theme change from selector
 * @param {Event} event - Change event
 */
function handleThemeChange(event) {
    const selectedTheme = event.target.value;
    applyTheme(selectedTheme);
    localStorage.setItem('theme', selectedTheme);
    updateThemeSelectors(selectedTheme);
}

/**
 * Update UI elements that need theme-specific styling
 * Called automatically when theme changes
 */
function updateThemeUI() {
    // Update auth notifications if they exist
    if (typeof updateAuthNotificationTheme === 'function') {
        updateAuthNotificationTheme();
    }
    
    // Update button styling if the function exists
    if (typeof updateButtonTheme === 'function') {
        updateButtonTheme();
    }
    
    // Update form elements with theme colors
    updateFormElements();
    
    // Trigger custom event for any other components that need to update
    document.dispatchEvent(new CustomEvent('theme:updated', { 
        detail: { theme: document.documentElement.getAttribute('data-theme') || 'green' } 
    }));
}

/**
 * Update form elements with theme colors
 * This ensures inputs, selects, and textareas have proper theming
 */
function updateFormElements() {
    // Apply theme variables to form elements
    const formElements = document.querySelectorAll('input, select, textarea');
    formElements.forEach(el => {
        // Only update elements that don't have specific theme classes already
        if (!el.classList.contains('theme-styled')) {
            el.style.borderColor = 'var(--border-color)';
            el.style.color = 'var(--text-color)';
            if (el.tagName === 'SELECT' || el.type === 'text' || el.type === 'password') {
                el.style.backgroundColor = 'var(--background-color)';
            }
        }
    });
}

/**
 * Set up all theme controls on the page
 */
function setupThemeControls() {
    // Set up theme toggle button
    const themeToggleButtons = document.querySelectorAll('#themeToggle');
    themeToggleButtons.forEach(button => {
        if (button) {
            // Remove existing listeners to prevent duplicates
            button.removeEventListener('click', handleThemeToggle);
            // Add click listener
            button.addEventListener('click', handleThemeToggle);
        }
    });
    
    // Set up theme selectors
    setupThemeSelector('themeSelector');
}

/**
 * Handle theme toggle button click
 * @param {Event} e - Click event
 */
function handleThemeToggle(e) {
    e.preventDefault();
    cycleTheme();
}

// Set up event listeners for theme changes
document.addEventListener('themechange', function() {
    updateThemeUI();
});

// Initialize theme when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    // Initialize theme
    const currentTheme = initTheme();
    
    // Update all selectors with current theme
    updateThemeSelectors(currentTheme);
    
    // Set up all theme controls
    setupThemeControls();
}); 