# Shobdo Client

This directory contains the client-side code for the Shobdo Bengali dictionary application.

## Auth0 Integration

### 1. Auth0 Setup

1. Create an [Auth0 account](https://auth0.com/signup)
2. Create a new Single Page Application
3. Configure your Auth0 application:
   - **Allowed Callback URLs**: `http://localhost:80/callback` (development), `https://yourdomain.com/callback` (production)
   - **Allowed Logout URLs**: `http://localhost:80`, `https://yourdomain.com`
   - **Allowed Web Origins**: `http://localhost:80`, `https://yourdomain.com`

### 2. Configure Frontend

Update `html/public/javascripts/config.js` with your Auth0 credentials:

```javascript
const AUTH0_CONFIG = {
    development: {
        domain: 'your-dev-tenant.auth0.com',  // Replace with actual Auth0 domain
        clientId: 'your-client-id',           // Replace with actual client ID
        audience: 'your-api-identifier'       // Optional: For API authorization
    },
    production: {
        domain: 'your-prod-tenant.auth0.com', // Replace with production Auth0 domain
        clientId: 'your-prod-client-id',      // Replace with production client ID
        audience: 'your-prod-api-identifier'  // Optional: For API authorization
    }
};
```

### 3. Local Testing

```bash
# Start Nginx with local configuration
docker-compose up -d

# Or manually with:
# nginx -c /path/to/nginx_local.conf

# Access the application
open http://localhost:80
```

### 4. Verify Configuration

1. Visit the Auth0 configuration checker: `http://localhost:80/auth-check`
2. The tool will validate your Auth0 settings and show any issues
3. Fix any configuration problems before testing the login flow

### 5. Test Authentication Flow

1. Click the "লগইন" (Login) button
2. Complete Auth0 authentication
3. After login, you should see your user profile in the top right
4. Access your profile page using the dropdown menu
5. Test logout functionality

## SEO Optimization Guide

The following SEO optimizations have been implemented to improve search engine visibility:

### Meta Tags and Structured Data
- Added comprehensive meta tags (description, keywords, author)
- Added Open Graph and Twitter Card tags for better social media sharing
- Implemented JSON-LD structured data for rich results
- Changed HTML lang attribute to "bn" (Bengali)
- Added canonical URL tag

### Semantic HTML
- Replaced generic `<div>` elements with semantic HTML5 elements (`<header>`, `<main>`, `<section>`, `<article>`, `<aside>`, `<footer>`)
- Added proper ARIA attributes and labels for accessibility
- Improved heading structure

### Performance Optimization
- Moved JavaScript to the end of the document
- Added proper caching for static assets
- Enabled gzip compression
- Added security headers

### SEO Files
- Added robots.txt
- Added sitemap.xml

### Nginx Configuration
- Enhanced with performance optimizations
- Added browser caching directives
- Added proper security headers

## Ongoing SEO Maintenance

To maintain good SEO performance, follow these practices:

### Regular Content Updates
```
# Update sitemap.xml lastmod date when content changes
find client/html/public -type f -name "*.html" -exec touch {} \;
sed -i '' "s/<lastmod>.*<\/lastmod>/<lastmod>$(date +%Y-%m-%d)<\/lastmod>/g" client/html/public/sitemap.xml
```

### Performance Monitoring
```
# Install and run Lighthouse CLI for performance auditing
npm install -g lighthouse
lighthouse https://www.shobdobaaz.com --output=html --output-path=./lighthouse-report.html
```

### Image Optimization
```
# Optimize images using imagemin
npm install -g imagemin-cli
imagemin client/html/public/images/* --out-dir=client/html/public/images/optimized
```

### Testing Mobile Responsiveness
```
# Use Chrome DevTools Device Mode
open -a "Google Chrome" https://www.shobdobaaz.com
# Then press F12 and click the mobile device icon
```

## Deployment

To deploy the optimized client:

```bash
# Build and deploy with Docker
make build-client
make deploy-client
```

## Troubleshooting

### Auth0 Issues
- **Login not working**: Verify callback URLs in Auth0 dashboard
- **API calls failing**: Check CORS headers and Authorization token
- **Redirect loops**: Ensure your Auth0 domain and client ID are correct

### File Structure

- `html/public/javascripts/auth.js` - Auth0 authentication logic
- `html/public/javascripts/config.js` - Environment configuration
- `html/public/callback.html` - Auth0 callback handling
- `html/public/profile.html` - User profile page
- `html/public/auth-check.html` - Auth0 configuration checker
