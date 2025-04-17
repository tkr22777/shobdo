FROM nginx:alpine

# Copy nginx configuration
COPY nginx_render.conf /etc/nginx/nginx.conf

# Copy website files
COPY html/public /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Start Nginx server
CMD ["nginx", "-g", "daemon off;"] 