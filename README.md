# 🚗 EV Dealer Management System - Frontend

Frontend React application cho hệ thống quản lý đại lý xe điện (EV Dealer Management System).

## 📋 Tổng quan

Ứng dụng React frontend được thiết kế để tương tác với backend Spring Boot API, cung cấp giao diện người dùng hiện đại và thân thiện cho việc quản lý toàn bộ quy trình bán xe điện.

## 🏗️ Kiến trúc hệ thống

### Công nghệ sử dụng
- **React 18** - Framework frontend
- **React Router DOM** - Điều hướng
- **Axios** - HTTP client
- **React Query** - State management và caching
- **React Hook Form** - Quản lý form
- **React Hot Toast** - Thông báo
- **Lucide React** - Icons
- **CSS3** - Styling

### Cấu trúc thư mục
```
src/
├── components/          # Các component tái sử dụng
│   ├── auth/           # Component xác thực
│   ├── common/         # Component chung
│   └── layout/         # Component layout
├── contexts/           # React Context
├── pages/              # Các trang chính
├── services/           # API services
└── styles/             # CSS files
```

## 🚀 Cài đặt và chạy

### Yêu cầu hệ thống
- Node.js 16+ 
- npm hoặc yarn
- Backend API đang chạy trên port 8080

### Cài đặt
```bash
# Clone repository
git clone <repository-url>
cd fe_EVDM

# Cài đặt dependencies
npm install

# Chạy ứng dụng
npm start
```

Ứng dụng sẽ chạy trên `http://localhost:3000`

## 🔐 Xác thực

### Tài khoản demo
- **Username**: `dealer_mgr_hcm`
- **Password**: `password`
- **Role**: `dealer_manager`

### Các vai trò người dùng
- **Admin** - Toàn quyền hệ thống
- **EVM Staff** - Quản lý đại lý và báo cáo
- **Dealer Manager** - Quản lý đơn hàng và khách hàng
- **Dealer Staff** - Tạo đơn hàng và báo giá

## 📱 Tính năng chính

### 1. Dashboard
- Tổng quan thống kê
- Thông báo quan trọng
- Thao tác nhanh
- Hoạt động gần đây

### 2. Quản lý xe
- **Thương hiệu**: Quản lý các thương hiệu xe
- **Dòng xe**: Quản lý các dòng xe theo thương hiệu
- **Phiên bản**: Quản lý các phiên bản xe
- **Màu sắc**: Quản lý màu sắc xe

### 3. Quản lý khách hàng
- Danh sách khách hàng
- Thông tin chi tiết
- Lịch sử giao dịch
- Tìm kiếm và lọc

### 4. Quy trình bán hàng
- **Báo giá**: Tạo và quản lý báo giá
- **Đơn hàng**: Xử lý đơn hàng
- **Hợp đồng**: Quản lý hợp đồng mua bán
- **Giao xe**: Lên lịch và theo dõi giao xe
- **Thanh toán**: Quản lý thanh toán

### 5. Quản lý kho
- **Kho**: Quản lý các kho xe
- **Tồn kho**: Theo dõi tồn kho xe

### 6. Báo cáo
- Báo cáo doanh số
- Báo cáo tồn kho
- Báo cáo khách hàng
- Báo cáo hiệu suất

### 7. Quản lý người dùng
- Danh sách người dùng
- Phân quyền
- Quản lý tài khoản

## 🎨 Giao diện

### Thiết kế
- **Responsive**: Tương thích mọi thiết bị
- **Modern UI**: Giao diện hiện đại, thân thiện
- **Dark/Light**: Hỗ trợ chế độ sáng/tối
- **Accessibility**: Tuân thủ tiêu chuẩn accessibility

### Màu sắc chủ đạo
- **Primary**: #667eea (Xanh dương)
- **Success**: #10b981 (Xanh lá)
- **Warning**: #f59e0b (Vàng)
- **Danger**: #ef4444 (Đỏ)
- **Gray**: #6b7280 (Xám)

## 🔧 Cấu hình

### Biến môi trường
Tạo file `.env` trong thư mục gốc:
```env
REACT_APP_API_URL=http://localhost:8080/api
```

### Proxy
Ứng dụng được cấu hình proxy đến backend:
```json
{
  "proxy": "http://localhost:8080"
}
```

## 📡 API Integration

### Authentication
- JWT token được lưu trong localStorage
- Tự động refresh token
- Interceptor xử lý lỗi 401

### API Services
- `authAPI` - Xác thực
- `vehicleAPI` - Quản lý xe
- `customerAPI` - Quản lý khách hàng
- `orderAPI` - Quản lý đơn hàng
- `reportAPI` - Báo cáo

## 🧪 Testing

```bash
# Chạy tests
npm test

# Chạy tests với coverage
npm test -- --coverage
```

## 📦 Build

```bash
# Build cho production
npm run build

# Build và serve
npm run build && npx serve -s build
```

## 🚀 Deployment

### Netlify
1. Build project: `npm run build`
2. Deploy thư mục `build/`

### Vercel
1. Connect repository
2. Deploy tự động

### Docker
```dockerfile
FROM node:16-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build
EXPOSE 3000
CMD ["npm", "start"]
```

## 🔒 Bảo mật

- JWT token authentication
- Role-based access control
- Input validation
- XSS protection
- CSRF protection

## 📱 Responsive Design

- **Mobile**: < 768px
- **Tablet**: 768px - 1024px
- **Desktop**: > 1024px

## 🎯 Performance

- Code splitting
- Lazy loading
- Image optimization
- Bundle optimization
- Caching strategy

## 🤝 Contributing

1. Fork repository
2. Tạo feature branch
3. Commit changes
4. Push to branch
5. Tạo Pull Request

## 📄 License

MIT License - xem file LICENSE để biết thêm chi tiết.

## 📞 Hỗ trợ

- **Repository**: [GitHub Repository]
- **Issues**: Tạo issue trong repository
- **Documentation**: [API Documentation]

## 🎉 Getting Started

1. **Đọc tài liệu** - Bắt đầu với README này
2. **Cài đặt môi trường** - Theo hướng dẫn cài đặt
3. **Chạy ứng dụng** - `npm start`
4. **Test APIs** - Sử dụng tài khoản demo
5. **Khám phá hệ thống** - Thử các tính năng khác nhau

Happy coding! 🚀
