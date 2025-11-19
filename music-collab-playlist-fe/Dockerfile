ARG NODE_VERSION=24.7.0-alpine
ARG NGINX_VERSION=alpine3.22

FROM node:${NODE_VERSION} AS builder

ARG GITHUB_TOKEN

WORKDIR /app

COPY package.json package-lock.json ./

RUN echo @hegpetac:registry=https://npm.pkg.github.com > .npmrc \
 && echo //npm.pkg.github.com/:_authToken=${GITHUB_TOKEN} >> .npmrc
RUN --mount=type=cache,target=/root/.npm npm ci
RUN rm .npmrc

COPY . .

RUN npm run build

FROM nginxinc/nginx-unprivileged:${NGINX_VERSION} AS runner

COPY nginx.conf /etc/nginx/nginx.conf

COPY --chown=nginx:nginx --from=builder /app/dist/*/browser /usr/share/nginx/html

EXPOSE 8080
