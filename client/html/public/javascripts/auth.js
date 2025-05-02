// Auth0 configuration
let auth0Client = null;
let isAuthenticated = false;
let userProfile = null;
let isAuthLoading = false;
let authError = null;

// Event listeners for auth state changes
const authListeners = [];
function addAuthStateListener(callback) {
    authListeners.push(callback);
}
function notifyAuthStateChange() {
    for (const listener of authListeners) {
        listener({
            isAuthenticated,
            isLoading: isAuthLoading,
            error: authError,
            userProfile
        });
    }
}

// Show auth notification message
function showAuthNotification(message, type = 'info', duration = 3000) {
    const $notification = $('#authNotification');
    
    // Use CSS variables for styling based on type and theme
    let bgColorVar, textColorVar;
    const isDarkTheme = document.documentElement.getAttribute('data-theme') === 'dark';
    
    switch (type) {
        case 'error':
            bgColorVar = isDarkTheme ? 'var(--notification-error-bg-dark)' : 'var(--notification-error-bg)';
            textColorVar = isDarkTheme ? 'var(--notification-error-text-dark)' : 'var(--notification-error-text)';
            break;
        case 'success':
            bgColorVar = isDarkTheme ? 'var(--notification-success-bg-dark)' : 'var(--notification-success-bg)';
            textColorVar = isDarkTheme ? 'var(--notification-success-text-dark)' : 'var(--notification-success-text)';
            break;
        case 'loading':
            bgColorVar = isDarkTheme ? 'var(--notification-loading-bg-dark)' : 'var(--notification-loading-bg)';
            textColorVar = isDarkTheme ? 'var(--notification-loading-text-dark)' : 'var(--notification-loading-text)';
            duration = 0; // Don't auto-hide for loading
            break;
        default: // info
            bgColorVar = isDarkTheme ? 'var(--notification-info-bg-dark)' : 'var(--notification-info-bg)';
            textColorVar = isDarkTheme ? 'var(--notification-info-text-dark)' : 'var(--notification-info-text)';
    }
    
    // Style and show notification
    $notification.css({
        backgroundColor: bgColorVar,
        color: textColorVar
    }).text(message).fadeIn();
    
    // Add spinner for loading
    if (type === 'loading') {
        $notification.html(`${message} <span class="auth-spinner"></span>`);
    }
    
    // Auto-hide after duration (if not loading)
    if (duration > 0) {
        setTimeout(() => {
            $notification.fadeOut();
        }, duration);
    }
}

// Function to update notification styling when theme changes
function updateAuthNotificationTheme() {
    const $notification = $('#authNotification');
    if (!$notification.is(':visible')) return;
    
    // Get current notification text
    const message = $notification.text();
    
    // Determine notification type from current styling
    const currentBg = $notification.css('backgroundColor');
    let type = 'info';
    
    // Simple check to guess the type based on current background
    if (currentBg.includes('rgb(248, 215, 218)') || currentBg.includes('rgb(231, 76, 60)')) {
        type = 'error';
    } else if (currentBg.includes('rgb(212, 237, 218)') || currentBg.includes('rgb(0, 188, 140)')) {
        type = 'success';
    } else if (currentBg.includes('rgb(226, 227, 229)') || currentBg.includes('rgb(68, 68, 68)')) {
        type = 'loading';
    }
    
    // Reapply with current message and type
    showAuthNotification(message, type);
}

// Hide notification
function hideAuthNotification() {
    $('#authNotification').fadeOut();
}

// Initialize the Auth0 client
async function initializeAuth() {
    try {
        isAuthLoading = true;
        notifyAuthStateChange();
        showAuthNotification('প্রমাণীকরণ চেক করা হচ্ছে...', 'loading');

        // Get Auth0 config based on current environment
        const config = getAuth0Config();
        
        auth0Client = await createAuth0Client({
            domain: config.domain,
            clientId: config.clientId,
            authorizationParams: {
                redirect_uri: window.location.origin + '/callback',
                audience: config.audience, // Optional: For calling secured API endpoints
            },
            cacheLocation: 'localstorage', // Options: 'memory', 'localstorage'
            useRefreshTokens: true, // Enable silent token refresh
        });

        // Check if the user is returning from Auth0 (handling the callback)
        if (window.location.search.includes("code=") && 
            window.location.search.includes("state=")) {
            try {
                // Handle the redirect from Auth0
                const result = await auth0Client.handleRedirectCallback();
                
                // You can access additional info if needed
                const appState = result?.appState;

                // Clear the URL parameters
                window.history.replaceState({}, document.title, appState?.returnTo || window.location.pathname);
                
                console.log("Successfully handled Auth0 callback");
                showAuthNotification('সফল লগইন!', 'success');
            } catch (callbackError) {
                console.error("Error handling Auth0 callback", callbackError);
                authError = "There was a problem logging you in. Please try again.";
                showAuthNotification('লগইন সমস্যা। আবার চেষ্টা করুন।', 'error');
            }
        }
        
        // Update authentication state
        await updateAuthState();
        
    } catch (error) {
        console.error("Error initializing Auth0 client", error);
        authError = "There was a problem connecting to the authentication service.";
        showAuthNotification('প্রমাণীকরণ সার্ভিসে সংযোগ করতে সমস্যা।', 'error');
    } finally {
        isAuthLoading = false;
        notifyAuthStateChange();
        hideAuthNotification();
    }
}

// Update authentication state and UI
async function updateAuthState() {
    try {
        isAuthLoading = true;
        authError = null;
        notifyAuthStateChange();
        
        isAuthenticated = await auth0Client.isAuthenticated();
        
        if (isAuthenticated) {
            userProfile = await auth0Client.getUser();
            
            // Store user info in sessionStorage (cleared when browser is closed)
            sessionStorage.setItem('userProfile', JSON.stringify(userProfile));
            
            // Update UI for logged-in user
            updateUI(true);
        } else {
            // Check if we have a stored profile from a previous session
            const storedProfile = sessionStorage.getItem('userProfile');
            if (storedProfile) {
                try {
                    userProfile = JSON.parse(storedProfile);
                    
                    // Verify if the stored token is still valid
                    // This is a simple check - the proper way is to validate with Auth0
                    const token = await getAccessTokenSilently().catch(() => null);
                    if (!token) {
                        sessionStorage.removeItem('userProfile');
                        userProfile = null;
                    }
                } catch (e) {
                    sessionStorage.removeItem('userProfile');
                    userProfile = null;
                }
            }
            
            // Update UI for guest
            updateUI(false);
        }
    } catch (error) {
        console.error("Error updating authentication state", error);
        authError = "Could not determine authentication status.";
    } finally {
        isAuthLoading = false;
        notifyAuthStateChange();
    }
}

// Login with Auth0
async function login() {
    try {
        isAuthLoading = true;
        authError = null;
        notifyAuthStateChange();
        showAuthNotification('লগইন পৃষ্ঠায় পুনঃনির্দেশিত করা হচ্ছে...', 'loading');
        
        // Store the current URL to return to after login
        await auth0Client.loginWithRedirect({
            appState: { 
                returnTo: window.location.pathname + window.location.search 
            }
        });
    } catch (error) {
        console.error("Login error", error);
        authError = "There was a problem initiating the login process.";
        isAuthLoading = false;
        notifyAuthStateChange();
        showAuthNotification('লগইন শুরু করতে সমস্যা।', 'error');
    }
}

// Logout from Auth0
async function logout() {
    try {
        isAuthLoading = true;
        authError = null;
        notifyAuthStateChange();
        showAuthNotification('লগআউট করা হচ্ছে...', 'loading');
        
        // Clear session storage
        sessionStorage.removeItem('userProfile');
        
        await auth0Client.logout({
            logoutParams: {
                returnTo: window.location.origin
            }
        });
    } catch (error) {
        console.error("Logout error", error);
        authError = "There was a problem logging out.";
        isAuthLoading = false;
        notifyAuthStateChange();
        showAuthNotification('লগআউট করতে সমস্যা।', 'error');
        
        // Force a page reload as a fallback
        window.location.reload();
    }
}

// Get access token for API calls with silent refresh if possible
async function getAccessTokenSilently() {
    try {
        return await auth0Client.getTokenSilently();
    } catch (error) {
        console.error("Error getting access token", error);
        
        // If the error is related to login required, you might want to redirect to login
        if (error.error === 'login_required') {
            authError = "Your session has expired. Please log in again.";
            notifyAuthStateChange();
            // Optionally trigger login flow
            await login();
        }
        
        throw error;
    }
}

// Update UI based on authentication state
function updateUI(isLoggedIn) {
    if (isLoggedIn && userProfile) {
        // Show user-specific elements
        $('#loginBtn').hide();
        $('#logoutBtn').removeClass('auth-hidden').show();
        $('#userProfile').text(userProfile.name || userProfile.email).removeClass('auth-hidden').show();
        
        // For API calls that require authentication
        setupAuthenticatedApiCalls();
    } else {
        // Show login button
        $('#loginBtn').show();
        $('#logoutBtn').addClass('auth-hidden').hide();
        $('#userProfile').addClass('auth-hidden').hide();
    }
    
    // Update loading state in UI if needed
    if (isAuthLoading) {
        $('#loginBtn').prop('disabled', true).text('অপেক্ষা করুন...');
        $('#logoutBtn').prop('disabled', true);
    } else {
        $('#loginBtn').prop('disabled', false).text('লগইন');
        $('#logoutBtn').prop('disabled', false);
    }
    
    // Update button styling based on current theme
    updateButtonTheme();
    
    // Display any authentication errors
    if (authError) {
        // You can display errors in a more user-friendly way
        console.error('Authentication error:', authError);
    }
    
    // Set up logout action in dropdown if it exists
    $('#logoutAction').off('click').on('click', function(e) {
        e.preventDefault();
        logout();
    });
}

// Update button styling based on theme
function updateButtonTheme() {
    // Define which buttons need dynamic styling updates
    const themeableButtons = {
        // Standard UI buttons
        '.btn-default': {
            'border-color': 'var(--border-color)',
            'color': 'var(--text-color)'
        },
        // Dropdown toggle buttons
        '.dropdown-toggle': {
            'border-color': 'var(--border-color)',
            'color': 'var(--text-color)'
        },
        // Special case for logout button which may need explicit styling
        '#logoutBtn': {
            'border-color': 'var(--border-color)', 
            'color': 'var(--text-color)'
        }
    };
    
    // Apply styles to all themeable buttons
    Object.entries(themeableButtons).forEach(([selector, styles]) => {
        const elements = $(selector);
        if (elements.length) {
            elements.css(styles);
        }
    });
    
    // Ensure the login button is using the .button class for styling
    // rather than direct style manipulation
    $('#loginBtn, #profileLoginBtn, .auth-action-btn').addClass('button');
}

// Set up authenticated API calls by adding the token to all AJAX requests
function setupAuthenticatedApiCalls() {
    // Add authorization header to all future AJAX requests
    $.ajaxSetup({
        beforeSend: async function(xhr, settings) {
            // Only add the token for API requests, not for loading static resources
            if (isAuthenticated && settings.url && 
                (settings.url.startsWith('/api/') || settings.url.includes('/secure/'))) {
                try {
                    const token = await getAccessTokenSilently();
                    xhr.setRequestHeader('Authorization', 'Bearer ' + token);
                } catch (error) {
                    console.error('Error setting Authorization header', error);
                    // Continue with the request even without the token
                }
            }
        }
    });
}

// Check if the token is about to expire and refresh it silently
// This can be called periodically to ensure the token stays fresh
async function refreshTokenIfNeeded() {
    if (isAuthenticated) {
        try {
            // This will attempt to refresh the token if it's close to expiration
            await getAccessTokenSilently();
        } catch (error) {
            console.error("Token refresh error", error);
            // Most errors are handled in getAccessTokenSilently
        }
    }
}

// Set up periodic token refresh if the user is active
function setupTokenRefresh() {
    // Check for token refresh every 5 minutes if the user is active
    const REFRESH_INTERVAL = 5 * 60 * 1000; // 5 minutes in milliseconds
    
    setInterval(() => {
        // Only attempt to refresh if the user has been active recently
        if (document.visibilityState === 'visible' && isAuthenticated) {
            refreshTokenIfNeeded();
        }
    }, REFRESH_INTERVAL);
    
    // Also refresh when the user returns to the tab
    document.addEventListener('visibilitychange', () => {
        if (document.visibilityState === 'visible' && isAuthenticated) {
            refreshTokenIfNeeded();
        }
    });
}

// Initialize when the document is ready
$(document).ready(function() {
    // Try to restore user profile from session storage first
    const storedProfile = sessionStorage.getItem('userProfile');
    if (storedProfile) {
        try {
            userProfile = JSON.parse(storedProfile);
            isAuthenticated = true;
            updateUI(true);
        } catch (e) {
            sessionStorage.removeItem('userProfile');
        }
    }
    
    // Initialize Auth0
    initializeAuth();
    
    // Set up event listeners for login/logout buttons
    $('#loginBtn').click(login);
    $('#logoutBtn').click(logout);
    
    // Set up periodic token refresh
    setupTokenRefresh();
    
    // Listen for theme changes to update notification styling
    document.addEventListener('themechange', updateAuthNotificationTheme);
    
    // Listen for theme changes to update button styling
    document.addEventListener('themechange', updateButtonTheme);
    
    // Apply initial button theme
    updateButtonTheme();
}); 
