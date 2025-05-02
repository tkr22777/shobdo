// Auth0 Configuration
const AUTH0_CONFIG = {
    development: {
        domain: 'your-tenant.auth0.com',        // Replace with your Auth0 tenant domain
        clientId: 'your-auth0-client-id',       // Replace with your Auth0 client ID
        audience: 'https://your-api-identifier', // Optional: For calling secured API endpoints
    },
    production: {
        domain: 'your-tenant.auth0.com',        // Replace with your Auth0 tenant domain
        clientId: 'your-auth0-client-id',       // Replace with your Auth0 client ID
        audience: 'https://your-api-identifier', // Optional: For calling secured API endpoints
    }
};

// Determine environment based on hostname
function getEnvironment() {
    const hostname = window.location.hostname;
    // Check if we're in a development environment
    if (hostname === 'localhost' || hostname === '127.0.0.1') {
        return 'development';
    }
    return 'production';
}

// Get the appropriate config based on environment
function getAuth0Config() {
    const env = getEnvironment();
    return AUTH0_CONFIG[env];
} 