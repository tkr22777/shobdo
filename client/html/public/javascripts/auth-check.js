/**
 * Auth0 Configuration Check Tool
 * 
 * Include this script after config.js and before auth.js to validate configuration.
 * Remove or comment out in production.
 */

(function() {
    // Get config based on environment
    const env = getEnvironment();
    const config = AUTH0_CONFIG[env];
    
    // Check if we're on the configuration page
    if (window.location.pathname === '/auth-check.html') {
        renderConfigCheck();
    } else {
        // Just do a basic check and log to console
        console.log('Auth0 Configuration Check:');
        checkConfig(config);
    }

    function renderConfigCheck() {
        const results = checkConfig(config);
        const resultContainer = document.getElementById('auth-config-results');
        
        if (!resultContainer) return;
        
        let html = '<ul class="list-group">';
        
        // Create result items
        results.forEach(result => {
            const statusClass = result.status ? 'list-group-item-success' : 'list-group-item-danger';
            html += `
                <li class="list-group-item ${statusClass}">
                    <h4 class="list-group-item-heading">${result.name}</h4>
                    <p class="list-group-item-text">${result.message}</p>
                </li>
            `;
        });
        
        html += '</ul>';
        
        // Display instructions if there are issues
        if (results.some(r => !r.status)) {
            html += `
                <div class="alert alert-warning mt-3">
                    <h4>Configuration Issues Detected</h4>
                    <p>Your Auth0 configuration needs to be updated in <code>config.js</code> before authentication will work.</p>
                </div>
            `;
        } else {
            html += `
                <div class="alert alert-success mt-3">
                    <h4>Configuration Looks Good!</h4>
                    <p>Your Auth0 configuration appears to be valid. Try the login flow to confirm everything works.</p>
                </div>
            `;
        }
        
        resultContainer.innerHTML = html;
    }

    function checkConfig(config) {
        const results = [];
        
        // Check domain
        results.push({
            name: 'Auth0 Domain',
            status: isValidDomain(config.domain),
            message: isValidDomain(config.domain) 
                ? `Domain is properly formatted: ${config.domain}` 
                : `Domain is not properly configured. Should be a valid Auth0 domain, not "${config.domain}"`
        });
        
        // Check clientId
        results.push({
            name: 'Client ID',
            status: isValidClientId(config.clientId),
            message: isValidClientId(config.clientId)
                ? `Client ID appears valid: ${config.clientId.substring(0, 5)}...`
                : `Client ID is not configured properly. Should be a valid Auth0 client ID, not "${config.clientId}"`
        });
        
        // Check audience if present
        if (config.audience) {
            results.push({
                name: 'API Audience (Optional)',
                status: isValidAudience(config.audience),
                message: isValidAudience(config.audience)
                    ? `API audience is configured: ${config.audience}`
                    : `API audience is configured but may not be valid: "${config.audience}"`
            });
        }
        
        // Check redirect URI (using current origin)
        const redirectUri = window.location.origin + '/callback';
        results.push({
            name: 'Redirect URI',
            status: true, // Can't validate this client-side
            message: `Verify this redirect URI is allowed in Auth0 dashboard: ${redirectUri}`
        });
        
        // Log results to console
        console.table(results.map(r => ({
            Check: r.name,
            Status: r.status ? 'PASS' : 'FAIL',
            Details: r.message
        })));
        
        return results;
    }

    // Helper functions
    function isValidDomain(domain) {
        if (!domain || domain === 'YOUR_AUTH0_DOMAIN' || domain.includes('your-') || domain === '') return false;
        return domain.includes('.auth0.com') || domain.includes('.eu.auth0.com') || domain.includes('.us.auth0.com');
    }
    
    function isValidClientId(clientId) {
        if (!clientId || clientId === 'YOUR_CLIENT_ID' || clientId.includes('your-') || clientId === '') return false;
        // Client IDs are generally alphanumeric and around 32 chars
        return clientId.length > 20; 
    }
    
    function isValidAudience(audience) {
        if (!audience || audience === 'YOUR_API_IDENTIFIER' || audience.includes('your-')) return false;
        return true;
    }
})(); 