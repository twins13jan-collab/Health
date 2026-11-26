/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  // The Java core (Spring Boot) is the system of record; the web app only
  // talks to it over REST (see lib/api.ts). No API routes are proxied here
  // by default — set NEXT_PUBLIC_API_BASE_URL at build/run time.
};

export default nextConfig;
