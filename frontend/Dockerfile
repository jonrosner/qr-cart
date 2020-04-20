# Stage 0 - Create production ready build
FROM node:8 as build-stage
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install
COPY . ./
RUN npm run build

# Stage 1 - Serve build files
FROM nginx:1.15
RUN rm /etc/nginx/conf.d/default.conf
COPY --from=build-stage /app/build /var/www/app
COPY --from=build-stage /app/nginx.conf /etc/nginx/conf.d/default.conf

