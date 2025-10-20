import React, { useState, useEffect } from 'react';
import { 
  Car, 
  Users, 
  ShoppingCart, 
  TrendingUp, 
  Package,
  DollarSign,
  Calendar,
  AlertCircle,
  FileText,
  UserPlus,
  BarChart3
} from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { 
  reportAPI, 
  orderAPI, 
  customerAPI, 
  inventoryAPI 
} from '../services/api';
import LoadingSpinner from '../components/common/LoadingSpinner';
import './Dashboard.css';

const Dashboard = () => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalOrders: 0,
    totalCustomers: 0,
    totalVehicles: 0,
    totalRevenue: 0,
    pendingOrders: 0,
    lowStock: 0
  });

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      
      // Load various data in parallel
      const [ordersResponse, customersResponse, inventoryResponse] = await Promise.allSettled([
        orderAPI.getOrders(),
        customerAPI.getCustomers(),
        inventoryAPI.getInventory()
      ]);

      // Process orders data
      let totalOrders = 0;
      let pendingOrders = 0;
      if (ordersResponse.status === 'fulfilled') {
        const orders = ordersResponse.value.data;
        totalOrders = orders.length;
        pendingOrders = orders.filter(order => order.status === 'pending').length;
      }

      // Process customers data
      let totalCustomers = 0;
      if (customersResponse.status === 'fulfilled') {
        totalCustomers = customersResponse.value.data.length;
      }

      // Process inventory data
      let totalVehicles = 0;
      let lowStock = 0;
      if (inventoryResponse.status === 'fulfilled') {
        const inventory = inventoryResponse.value.data;
        totalVehicles = inventory.length;
        lowStock = inventory.filter(item => item.status === 'available').length;
      }

      setStats({
        totalOrders,
        totalCustomers,
        totalVehicles,
        totalRevenue: 0, // This would come from a revenue report
        pendingOrders,
        lowStock
      });

    } catch (error) {
      console.error('Error loading dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const statCards = [
    {
      title: 'Tổng đơn hàng',
      value: stats.totalOrders,
      icon: ShoppingCart,
      color: 'blue',
      change: '+12%',
      changeType: 'positive'
    },
    {
      title: 'Khách hàng',
      value: stats.totalCustomers,
      icon: Users,
      color: 'green',
      change: '+8%',
      changeType: 'positive'
    },
    {
      title: 'Xe trong kho',
      value: stats.totalVehicles,
      icon: Car,
      color: 'purple',
      change: '-3%',
      changeType: 'negative'
    },
    {
      title: 'Doanh thu',
      value: `${(stats.totalRevenue / 1000000).toFixed(1)}M VNĐ`,
      icon: DollarSign,
      color: 'orange',
      change: '+15%',
      changeType: 'positive'
    }
  ];

  const quickActions = [
    {
      title: 'Tạo báo giá mới',
      description: 'Tạo báo giá cho khách hàng',
      icon: FileText,
      color: 'blue',
      path: '/quotations/new'
    },
    {
      title: 'Thêm khách hàng',
      description: 'Đăng ký khách hàng mới',
      icon: UserPlus,
      color: 'green',
      path: '/customers/new'
    },
    {
      title: 'Quản lý kho',
      description: 'Kiểm tra tồn kho xe',
      icon: Package,
      color: 'purple',
      path: '/inventory'
    },
    {
      title: 'Xem báo cáo',
      description: 'Báo cáo doanh số và hiệu suất',
      icon: BarChart3,
      color: 'orange',
      path: '/reports'
    }
  ];

  if (loading) {
    return <LoadingSpinner text="Đang tải dữ liệu dashboard..." />;
  }

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h1>Chào mừng, {user?.username}!</h1>
        <p>Đây là tổng quan về hệ thống quản lý đại lý xe điện</p>
      </div>

      {/* Stats Cards */}
      <div className="stats-grid">
        {statCards.map((stat, index) => (
          <div key={index} className={`stat-card stat-card-${stat.color}`}>
            <div className="stat-icon">
              <stat.icon size={24} />
            </div>
            <div className="stat-content">
              <div className="stat-value">{stat.value}</div>
              <div className="stat-title">{stat.title}</div>
              <div className={`stat-change stat-change-${stat.changeType}`}>
                {stat.change}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Alerts */}
      {(stats.pendingOrders > 0 || stats.lowStock < 10) && (
        <div className="alerts-section">
          <h3>Thông báo quan trọng</h3>
          <div className="alerts-grid">
            {stats.pendingOrders > 0 && (
              <div className="alert alert-warning">
                <AlertCircle size={20} />
                <div>
                  <strong>{stats.pendingOrders} đơn hàng</strong> đang chờ xử lý
                </div>
              </div>
            )}
            {stats.lowStock < 10 && (
              <div className="alert alert-danger">
                <AlertCircle size={20} />
                <div>
                  <strong>Kho sắp hết xe</strong> - Chỉ còn {stats.lowStock} xe
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Quick Actions */}
      <div className="quick-actions-section">
        <h3>Thao tác nhanh</h3>
        <div className="quick-actions-grid">
          {quickActions.map((action, index) => (
            <div key={index} className={`quick-action quick-action-${action.color}`}>
              <div className="quick-action-icon">
                <action.icon size={24} />
              </div>
              <div className="quick-action-content">
                <h4>{action.title}</h4>
                <p>{action.description}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Recent Activity */}
      <div className="recent-activity-section">
        <h3>Hoạt động gần đây</h3>
        <div className="activity-list">
          <div className="activity-item">
            <div className="activity-icon">
              <ShoppingCart size={16} />
            </div>
            <div className="activity-content">
              <div className="activity-title">Đơn hàng mới #ORD-001</div>
              <div className="activity-time">2 giờ trước</div>
            </div>
          </div>
          <div className="activity-item">
            <div className="activity-icon">
              <Users size={16} />
            </div>
            <div className="activity-content">
              <div className="activity-title">Khách hàng mới đăng ký</div>
              <div className="activity-time">4 giờ trước</div>
            </div>
          </div>
          <div className="activity-item">
            <div className="activity-icon">
              <Car size={16} />
            </div>
            <div className="activity-content">
              <div className="activity-title">Xe mới nhập kho</div>
              <div className="activity-time">6 giờ trước</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
