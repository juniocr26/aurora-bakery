# syntax=docker/dockerfile:1
FROM node:22.23.3-bookworm-slim@sha256:43ac6c60b8f89723f746e8a92ce91abd5017e627ce1ddfe4238355d3a30b772c AS development-base
LABEL org.opencontainers.image.title="Aurora Bakery Frontend"
WORKDIR /app
RUN chown node:node /app
USER node
FROM development-base AS development
COPY --chown=node:node package.json package-lock.json ./
RUN npm ci --no-fund
COPY --chown=node:node . .
EXPOSE 4200
CMD ["npm", "start", "--", "--poll", "1000"]

FROM development AS build
RUN npm run build
