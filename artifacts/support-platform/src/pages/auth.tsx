import { type FormEvent, type ReactNode, useState } from 'react';
import { ArrowRight, Eye, EyeOff, LockKeyhole, Mail, ShieldCheck, UserRound } from 'lucide-react';
import { Link, useLocation } from 'wouter';
import { useLogin, useRegister } from '@workspace/api-client-react';
import { Brand } from '@/components/app-shell';
import { Button, cx } from '@/components/ui';

function AuthFrame({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-[100dvh] bg-background md:grid md:grid-cols-[1.05fr_.95fr]">
      <div className="relative hidden overflow-hidden bg-sidebar p-12 text-sidebar-foreground md:flex md:flex-col md:justify-between">
        <div className="absolute inset-0 surface-grid opacity-20" />
        <div className="relative">
          <Brand light />
          <div className="mt-28 max-w-lg">
            <p className="font-mono-ui text-[11px] uppercase tracking-[.2em] text-sidebar-primary">A calmer queue starts here</p>
            <h1 className="mt-5 text-5xl font-extrabold leading-[1.02] tracking-[-.065em]">Know the next move<br /><span className="text-sidebar-primary">before you make it.</span></h1>
            <p className="mt-6 max-w-md text-sm leading-7 text-sidebar-foreground/60">Northstar keeps customer context, team judgment, and grounded Gemini intelligence in one focused workspace.</p>
            <div className="mt-10 grid max-w-md grid-cols-2 gap-3">
              <div className="rounded-2xl border border-sidebar-border bg-sidebar-accent/45 p-4"><p className="font-mono-ui text-2xl text-sidebar-primary">01</p><p className="mt-5 text-xs font-bold">Read the signal</p><p className="mt-1 text-xs text-sidebar-foreground/50">Every ticket, one clear pulse.</p></div>
              <div className="rounded-2xl border border-sidebar-border bg-sidebar-accent/45 p-4"><p className="font-mono-ui text-2xl text-sidebar-primary">02</p><p className="mt-5 text-xs font-bold">Make it human</p><p className="mt-1 text-xs text-sidebar-foreground/50">AI stays anchored to your docs.</p></div>
            </div>
          </div>
        </div>
      </div>
      <div className="flex items-center justify-center p-6 md:p-12">{children}</div>
    </div>
  );
}

export function AuthPage({ mode }: { mode: 'login' | 'register' }) {
  const [, setLocation] = useLocation();
  const login = useLogin();
  const register = useRegister();
  const [showPassword, setShowPassword] = useState(false);
  const [form, setForm] = useState({ name: '', email: '', password: '' });
  const mutation = mode === 'login' ? login : register;
  const submit = (event: FormEvent) => {
    event.preventDefault();
    const onSuccess = (result: { token: string; user: { name: string; email: string; role: string } }) => {
      localStorage.setItem('support_token', result.token);
      localStorage.setItem('support_user_name', result.user.name);
      localStorage.setItem('support_user_email', result.user.email);
      localStorage.setItem('support_user_role', result.user.role);
      setLocation('/');
    };
    if (mode === 'login') login.mutate({ data: { email: form.email, password: form.password } }, { onSuccess });
    else register.mutate({ data: form }, { onSuccess });
  };
  return (
    <AuthFrame>
      <div className="w-full max-w-[420px] animate-rise">
        <div className="mb-9 md:hidden"><Brand /></div>
        <div className="mb-8">
          <p className="font-mono-ui text-[11px] uppercase tracking-[.18em] text-primary">{mode === 'login' ? 'Welcome back' : 'Start with signal'}</p>
          <h2 className="mt-3 text-3xl font-extrabold tracking-[-.05em]">{mode === 'login' ? 'Sign in to Northstar' : 'Create your workspace'}</h2>
          <p className="mt-2 text-sm text-muted-foreground">{mode === 'login' ? 'Your queue is waiting. Let’s make it lighter.' : 'A focused home for every customer conversation.'}</p>
        </div>
        <form onSubmit={submit} className="space-y-4">
          {mode === 'register' && <Field label="Your name" icon={<UserRound size={16} />} value={form.name} onChange={(value) => setForm({ ...form, name: value })} placeholder="Maya Chen" testId="input-name" />}
          <Field label="Work email" icon={<Mail size={16} />} value={form.email} onChange={(value) => setForm({ ...form, email: value })} placeholder="you@company.com" type="email" testId="input-email" />
          <div>
            <label className="mb-1.5 block text-xs font-bold">Password</label>
            <div className="relative">
              <LockKeyhole size={16} className="absolute left-3 top-3 text-muted-foreground" />
              <input data-testid="input-password" required minLength={mode === 'register' ? 8 : 1} type={showPassword ? 'text' : 'password'} value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} className="h-11 w-full rounded-lg border border-input bg-card pl-10 pr-11 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15" placeholder={mode === 'register' ? 'At least 8 characters' : 'Enter your password'} />
              <button type="button" data-testid="button-toggle-password" onClick={() => setShowPassword(!showPassword)} className="absolute right-3 top-2.5 rounded p-1 text-muted-foreground hover:text-foreground">{showPassword ? <EyeOff size={16} /> : <Eye size={16} />}</button>
            </div>
          </div>
          {mutation.isError && <p data-testid="status-auth-error" className="rounded-lg bg-destructive/8 px-3 py-2 text-xs font-semibold text-destructive">We could not verify those details. Check them and try again.</p>}
          <Button type="submit" loading={mutation.isPending} className="mt-2 h-12 w-full">{mode === 'login' ? 'Enter workspace' : 'Create account'} <ArrowRight size={16} /></Button>
        </form>
        <div className="mt-7 flex items-center justify-center gap-2 text-xs text-muted-foreground"><ShieldCheck size={14} className="text-primary" />Your workspace is private by default</div>
        <p className="mt-8 text-center text-sm text-muted-foreground">{mode === 'login' ? 'New to Northstar?' : 'Already have an account?'} <Link href={mode === 'login' ? '/register' : '/login'} data-testid="link-auth-switch" className="font-bold text-primary hover:underline">{mode === 'login' ? 'Create an account' : 'Sign in'}</Link></p>
      </div>
    </AuthFrame>
  );
}

function Field({ label, icon, value, onChange, placeholder, type = 'text', testId }: { label: string; icon: ReactNode; value: string; onChange: (value: string) => void; placeholder: string; type?: string; testId: string }) {
  return <div><label className="mb-1.5 block text-xs font-bold">{label}</label><div className="relative"><span className="absolute left-3 top-3 text-muted-foreground">{icon}</span><input data-testid={testId} required type={type} value={value} onChange={(e) => onChange(e.target.value)} placeholder={placeholder} className={cx('h-11 w-full rounded-lg border border-input bg-card pl-10 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15', type === 'email' && 'lowercase')} /></div></div>;
}