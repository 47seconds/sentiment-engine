# Driver Sentiment Engine - Frontend

Beautiful, modern React dashboard for the Driver Sentiment Analysis system.

## 🚀 Tech Stack

- **React 18** - UI framework
- **Vite** - Fast build tool
- **Material-UI (MUI)** - Component library
- **Recharts** - Charts and graphs
- **React Router** - Navigation
- **Axios** - HTTP client
- **React Hot Toast** - Notifications

## 📁 Project Structure

```
frontend/
├── src/
│   ├── components/
│   │   ├── layout/          # Header, Sidebar, MainLayout
│   │   ├── dashboard/       # Dashboard widgets
│   │   ├── charts/          # Recharts components
│   │   ├── feedback/        # Feedback form components
│   │   └── common/          # Reusable components
│   ├── pages/               # Route pages
│   ├── services/            # API services
│   ├── styles/              # Theme & global styles
│   └── utils/               # Helper functions
├── public/
└── package.json
```

## 🎨 Design System

### Color Palette
- **Primary**: Deep Blue (#1e3a8a) - Trust, professionalism
- **Success**: Green (#10b981) - Positive sentiment
- **Warning**: Amber (#f59e0b) - Needs attention
- **Error**: Red (#ef4444) - Critical alerts
- **Secondary**: Purple (#7c3aed) - Accent

### Typography
- **Font**: Inter (modern, clean)
- **Weights**: 300, 400, 500, 600, 700

### Components
- **Border Radius**: 8-12px for rounded corners
- **Shadows**: Subtle elevation (0-6 levels)
- **Spacing**: Consistent padding/margins

## 🛠️ Setup

### Install Dependencies
```bash
cd frontend
npm install
```

### Run Development Server
```bash
npm run dev
```

The app will run on **http://localhost:3000**

### Build for Production
```bash
npm run build
```

## 🔌 API Integration

The frontend connects to the backend API at **http://localhost:8080/api**

API endpoints:
- `/stats` - Driver statistics
- `/feedback` - Feedback submissions
- `/alerts` - Alert management
- `/drivers` - Driver data
- `/admin` - Configuration

## 🌙 Features

- ✅ **Responsive Design** - Mobile, tablet, desktop
- ✅ **Dark Mode** - Toggle between light/dark themes
- ✅ **Real-time Updates** - Live sentiment scores
- ✅ **Interactive Charts** - Recharts visualization
- ✅ **Notifications** - Toast messages for actions
- ✅ **Navigation** - Clean sidebar with active states
- ✅ **Accessibility** - WCAG compliant

## 📊 Pages

1. **Dashboard** - KPIs, charts, recent activity
2. **Drivers** - Searchable driver table with sentiment scores
3. **Alerts** - Active alerts with severity filtering
4. **Feedback** - Submit new feedback form
5. **Admin** - Feature flags, threshold configuration

## 🎯 Next Steps

- [ ] Complete Dashboard page with charts
- [ ] Build Driver table with search/filter
- [ ] Create Feedback form with validation
- [ ] Implement Alert management
- [ ] Add Admin configuration panel

---

**Status**: ✅ Phase 1 Complete - Layout & Navigation Ready
