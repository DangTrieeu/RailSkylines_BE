# Pusher WebSocket Integration for Articles

## Backend Configuration

### Channel and Event Names
- **Channel**: `articles-channel`
- **Event**: `article-event`

### Event Types
```typescript
enum EventType {
  CREATED = "CREATED",
  UPDATED = "UPDATED",
  DELETED = "DELETED"
}
```

### Event Data Structure
```typescript
interface ArticleEvent {
  eventType: "CREATED" | "UPDATED" | "DELETED";
  articleId: number;
  title: string | null;
  content: string | null;
  thumbnail: string | null;
  userId: number | null;
  userName: string | null;
  timestamp: string; // ISO 8601 format
}
```

## Frontend Integration (Next.js)

### 1. Install Pusher Client
```bash
npm install pusher-js
# or
yarn add pusher-js
```

### 2. Create Pusher Hook (hooks/usePusher.ts)
```typescript
import { useEffect, useState } from 'react';
import Pusher from 'pusher-js';

interface ArticleEvent {
  eventType: 'CREATED' | 'UPDATED' | 'DELETED';
  articleId: number;
  title: string | null;
  content: string | null;
  thumbnail: string | null;
  userId: number | null;
  userName: string | null;
  timestamp: string;
}

export const usePusher = (onArticleEvent: (event: ArticleEvent) => void) => {
  const [pusher, setPusher] = useState<Pusher | null>(null);

  useEffect(() => {
    // Initialize Pusher
    const pusherInstance = new Pusher('95fc976d5de6139cdebe', {
      cluster: 'ap1',
    });

    // Subscribe to channel
    const channel = pusherInstance.subscribe('articles-channel');

    // Bind to event
    channel.bind('article-event', (data: string) => {
      const event: ArticleEvent = JSON.parse(data);
      onArticleEvent(event);
    });

    setPusher(pusherInstance);

    // Cleanup
    return () => {
      channel.unbind_all();
      channel.unsubscribe();
      pusherInstance.disconnect();
    };
  }, [onArticleEvent]);

  return pusher;
};
```

### 3. Usage Example in Component
```typescript
'use client';

import { useState, useCallback } from 'react';
import { usePusher } from '@/hooks/usePusher';

interface Article {
  articleId: number;
  title: string;
  content: string;
  thumbnail: string;
  userName: string;
}

export default function ArticlesPage() {
  const [articles, setArticles] = useState<Article[]>([]);
  const [notification, setNotification] = useState('');

  // Handle article events from Pusher
  const handleArticleEvent = useCallback((event: any) => {
    console.log('Received event:', event);

    switch (event.eventType) {
      case 'CREATED':
        setNotification(`New article created: ${event.title}`);
        // Refresh article list or add new article
        setArticles(prev => [...prev, {
          articleId: event.articleId,
          title: event.title,
          content: event.content,
          thumbnail: event.thumbnail,
          userName: event.userName
        }]);
        break;

      case 'UPDATED':
        setNotification(`Article updated: ${event.title}`);
        // Update article in list
        setArticles(prev => prev.map(article => 
          article.articleId === event.articleId 
            ? { ...article, title: event.title, content: event.content, thumbnail: event.thumbnail }
            : article
        ));
        break;

      case 'DELETED':
        setNotification(`Article deleted`);
        // Remove article from list
        setArticles(prev => prev.filter(article => article.articleId !== event.articleId));
        break;
    }

    // Clear notification after 5 seconds
    setTimeout(() => setNotification(''), 5000);
  }, []);

  // Initialize Pusher
  usePusher(handleArticleEvent);

  return (
    <div>
      {notification && (
        <div className="notification">
          {notification}
        </div>
      )}
      
      <h1>Articles (Real-time)</h1>
      <div className="articles-grid">
        {articles.map(article => (
          <div key={article.articleId} className="article-card">
            <h2>{article.title}</h2>
            <p>{article.content}</p>
            <span>By: {article.userName}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
```

### 4. Alternative: Using Context Provider
```typescript
// contexts/PusherContext.tsx
'use client';

import { createContext, useContext, useEffect, useState } from 'react';
import Pusher from 'pusher-js';

interface PusherContextType {
  pusher: Pusher | null;
}

const PusherContext = createContext<PusherContextType>({ pusher: null });

export const PusherProvider = ({ children }: { children: React.ReactNode }) => {
  const [pusher, setPusher] = useState<Pusher | null>(null);

  useEffect(() => {
    const pusherInstance = new Pusher('95fc976d5de6139cdebe', {
      cluster: 'ap1',
    });

    setPusher(pusherInstance);

    return () => {
      pusherInstance.disconnect();
    };
  }, []);

  return (
    <PusherContext.Provider value={{ pusher }}>
      {children}
    </PusherContext.Provider>
  );
};

export const usePusherContext = () => useContext(PusherContext);
```

### 5. Environment Variables (.env.local)
```env
NEXT_PUBLIC_PUSHER_KEY=95fc976d5de6139cdebe
NEXT_PUBLIC_PUSHER_CLUSTER=ap1
```

Then use in code:
```typescript
const pusherInstance = new Pusher(process.env.NEXT_PUBLIC_PUSHER_KEY!, {
  cluster: process.env.NEXT_PUBLIC_PUSHER_CLUSTER!,
});
```

## Testing

### Test with Pusher Debug Console
1. Go to [Pusher Dashboard](https://dashboard.pusher.com/)
2. Select your app
3. Go to "Debug Console"
4. You'll see events in real-time when articles are created/updated/deleted

### Test Backend
```bash
# Create article
curl -X POST http://localhost:8080/api/v1/articles \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Article",
    "content": "This is a test",
    "thumbnail": "http://example.com/image.jpg"
  }'
```

Check the Pusher Debug Console or your Next.js app to see the event.

## Notes
- Events are sent automatically when articles are created, updated, or deleted
- The channel is public (no authentication required)
- Events include full article data for easy FE updates
- Timestamp is in ISO 8601 format (UTC)

