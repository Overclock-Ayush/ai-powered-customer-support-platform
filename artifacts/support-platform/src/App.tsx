import { type ReactNode } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Route, Switch, Router as WouterRouter, useLocation } from 'wouter';
import { ErrorBoundary } from '@/components/error-boundary';
import { Toaster } from '@/components/ui/toaster';
import { TooltipProvider } from '@/components/ui/tooltip';
import { AppShell } from '@/components/app-shell';
import { AuthPage } from '@/pages/auth';
import { DashboardPage } from '@/pages/dashboard';
import { KnowledgePage } from '@/pages/knowledge';
import { NewTicketPage, TicketsPage } from '@/pages/tickets';
import { TicketDetailPage } from '@/pages/ticket-detail';
import { SettingsPage } from '@/pages/settings';
import NotFound from '@/pages/not-found';
import { setAuthTokenGetter, setBaseUrl } from '@workspace/api-client-react';

const queryClient = new QueryClient({ defaultOptions: { queries: { retry: 1, staleTime: 15_000 } } });
setBaseUrl(import.meta.env.VITE_API_BASE_URL ?? null);
setAuthTokenGetter(() => localStorage.getItem('support_token'));

function RoutedErrorBoundary({ children }: { children: ReactNode }) {
  const [location] = useLocation();
  return <ErrorBoundary resetKey={location}>{children}</ErrorBoundary>;
}

function AuthRoutes() {
  return <Switch><Route path="/login"><AuthPage mode="login" /></Route><Route path="/register"><AuthPage mode="register" /></Route><Route component={NotFound} /></Switch>;
}

function WorkspaceRoutes() {
  return <AppShell><RoutedErrorBoundary><Switch><Route path="/" component={DashboardPage} /><Route path="/tickets" component={TicketsPage} /><Route path="/tickets/new" component={NewTicketPage} /><Route path="/tickets/:id" component={TicketDetailPage} /><Route path="/knowledge" component={KnowledgePage} /><Route path="/settings" component={SettingsPage} /><Route component={NotFound} /></Switch></RoutedErrorBoundary></AppShell>;
}

function Router() {
  return <Switch><Route path="/login" component={AuthRoutes} /><Route path="/register" component={AuthRoutes} /><Route component={WorkspaceRoutes} /></Switch>;
}

function App() {
  return <QueryClientProvider client={queryClient}><TooltipProvider><WouterRouter base={import.meta.env.BASE_URL.replace(/\/$/, '')}><Router /></WouterRouter><Toaster /></TooltipProvider></QueryClientProvider>;
}

export default App;