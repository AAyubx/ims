export class DateTimeUtil {
  static formatLastLogin(lastLogin: string): {
    relative: string;
    absolute: string;
    timezone: string;
  } {
    const date = new Date(lastLogin);
    const userTimezone = Intl.DateTimeFormat().resolvedOptions().timeZone;
    
    return {
      relative: this.getRelativeTime(date),
      absolute: date.toLocaleString('en-US', {
        timeZone: userTimezone,
        dateStyle: 'medium',
        timeStyle: 'short'
      }),
      timezone: userTimezone
    };
  }

  private static getRelativeTime(date: Date): string {
    const rtf = new Intl.RelativeTimeFormat('en', { numeric: 'auto' });
    const diffInMs = date.getTime() - Date.now();
    const diffInHours = Math.round(diffInMs / (1000 * 60 * 60));
    
    if (Math.abs(diffInHours) < 24) {
      return rtf.format(diffInHours, 'hour');
    } else {
      const diffInDays = Math.round(diffInHours / 24);
      return rtf.format(diffInDays, 'day');
    }
  }

  static formatLastLoginMessage(lastLoginAt: string | null): string {
    if (!lastLoginAt) {
      return "Welcome! This is your first login.";
    }
    
    try {
      const { relative, absolute } = this.formatLastLogin(lastLoginAt);
      return `Last login: ${relative} (${absolute})`;
    } catch (error) {
      console.warn('Error formatting last login date:', error);
      return "Welcome back!";
    }
  }
}