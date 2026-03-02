# Shobdo Client

This directory contains the client-side code for the Shobdo Bengali dictionary application.

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
lighthouse https://www.shobdo.info --output=html --output-path=./lighthouse-report.html
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
open -a "Google Chrome" https://www.shobdo.info
# Then press F12 and click the mobile device icon
```

## Deployment

To deploy the optimized client:

```
# Build and deploy with Docker
make build-client
make deploy-client
``` 