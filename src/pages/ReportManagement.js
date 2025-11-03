import React, { useState, useEffect } from 'react';
import { BarChart3, Download, Filter, TrendingUp, Users, Package, DollarSign } from 'lucide-react';
import { reportAPI } from '../services/api';
import LoadingSpinner from '../components/common/LoadingSpinner';
import toast from 'react-hot-toast';
import '../styles/common.css';
import './ReportManagement.css';

const ReportManagement = () => {
  const [loading, setLoading] = useState(false);
  const [reports, setReports] = useState({
    salesSummary: null,
    inventoryTurnover: null,
    customerDebt: null,
    monthlySales: null,
    dealerPerformance: null
  });
  const [filters, setFilters] = useState({
    startDate: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().split('T')[0],
    endDate: new Date().toISOString().split('T')[0],
    reportType: 'all',
    dealer: 'all'
  });

  useEffect(() => {
    loadReports();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters]);

  const loadReports = async () => {
    try {
      setLoading(true);
      const [salesSummary, inventoryTurnover, customerDebt, monthlySales, dealerPerformance] = await Promise.all([
        reportAPI.getSalesSummary(filters.startDate, filters.endDate),
        reportAPI.getInventoryTurnover(),
        reportAPI.getCustomerDebt(),
        reportAPI.getMonthlySales(),
        reportAPI.getDealerPerformance()
      ]);

      setReports({
        salesSummary: salesSummary.data,
        inventoryTurnover: inventoryTurnover.data,
        customerDebt: customerDebt.data,
        monthlySales: monthlySales.data,
        dealerPerformance: dealerPerformance.data
      });
    } catch (error) {
      console.error('Error loading reports:', error);
      toast.error('Không thể tải báo cáo');
    } finally {
      setLoading(false);
    }
  };

  const handleExportReport = async (reportType) => {
    try {
      toast(`Xuất báo cáo ${reportType}...`, { icon: '📊' });
      // TODO: Implement export functionality
      // This would typically generate and download a PDF/Excel file
    } catch (error) {
      console.error('Error exporting report:', error);
      toast.error('Không thể xuất báo cáo');
    }
  };

  const handleFilterChange = (key, value) => {
    setFilters(prev => ({
      ...prev,
      [key]: value
    }));
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  if (loading) {
    return <LoadingSpinner text="Đang tải báo cáo..." />;
  }

  return (
    <div className="report-management">
      <div className="page-header">
        <div className="page-title">
          <BarChart3 className="title-icon" />
          <h1>Báo cáo & Phân tích</h1>
        </div>
        <p>Báo cáo doanh số, hiệu suất và phân tích dữ liệu</p>
      </div>

      <div className="filters-section">
        <div className="filters-header">
          <h3>Bộ lọc báo cáo</h3>
          <button className="btn btn-primary" onClick={loadReports}>
            <Filter size={20} />
            Áp dụng bộ lọc
          </button>
        </div>
        <div className="filters-content">
          <div className="filter-group">
            <label>Từ ngày</label>
            <input 
              type="date" 
              className="form-input"
              value={filters.startDate}
              onChange={(e) => handleFilterChange('startDate', e.target.value)}
            />
          </div>
          <div className="filter-group">
            <label>Đến ngày</label>
            <input 
              type="date" 
              className="form-input"
              value={filters.endDate}
              onChange={(e) => handleFilterChange('endDate', e.target.value)}
            />
          </div>
          <div className="filter-group">
            <label>Loại báo cáo</label>
            <select 
              className="form-select"
              value={filters.reportType}
              onChange={(e) => handleFilterChange('reportType', e.target.value)}
            >
              <option value="all">Tất cả</option>
              <option value="sales">Doanh số</option>
              <option value="inventory">Tồn kho</option>
              <option value="customer">Khách hàng</option>
            </select>
          </div>
        </div>
      </div>

      <div className="reports-grid">
        <div className="report-card">
          <div className="report-header">
            <h3>Báo cáo doanh số</h3>
            <button 
              className="btn btn-outline btn-sm"
              onClick={() => handleExportReport('doanh số')}
            >
              <Download size={16} />
              Xuất Excel
            </button>
          </div>
          <div className="report-content">
            <div className="report-stats">
              <div className="stat">
                <div className="stat-value">
                  {reports.salesSummary?.totalRevenue ? formatCurrency(reports.salesSummary.totalRevenue) : 'N/A'}
                </div>
                <div className="stat-label">Tổng doanh thu</div>
              </div>
              <div className="stat">
                <div className="stat-value">
                  {reports.salesSummary?.totalOrders || 0}
                </div>
                <div className="stat-label">Tổng đơn hàng</div>
              </div>
            </div>
            <div className="report-chart">
              <div className="chart-placeholder">
                <DollarSign size={48} />
                <p>Biểu đồ doanh số</p>
              </div>
            </div>
          </div>
        </div>

        <div className="report-card">
          <div className="report-header">
            <h3>Báo cáo tồn kho</h3>
            <button 
              className="btn btn-outline btn-sm"
              onClick={() => handleExportReport('tồn kho')}
            >
              <Download size={16} />
              Xuất Excel
            </button>
          </div>
          <div className="report-content">
            <div className="report-stats">
              <div className="stat">
                <div className="stat-value">
                  {reports.inventoryTurnover?.totalVehicles || 0}
                </div>
                <div className="stat-label">Tổng xe trong kho</div>
              </div>
              <div className="stat">
                <div className="stat-value">
                  {reports.inventoryTurnover?.soldVehicles || 0}
                </div>
                <div className="stat-label">Xe đã bán</div>
              </div>
            </div>
            <div className="report-chart">
              <div className="chart-placeholder">
                <Package size={48} />
                <p>Biểu đồ tồn kho</p>
              </div>
            </div>
          </div>
        </div>

        <div className="report-card">
          <div className="report-header">
            <h3>Báo cáo khách hàng</h3>
            <button 
              className="btn btn-outline btn-sm"
              onClick={() => handleExportReport('khách hàng')}
            >
              <Download size={16} />
              Xuất Excel
            </button>
          </div>
          <div className="report-content">
            <div className="report-stats">
              <div className="stat">
                <div className="stat-value">
                  {reports.customerDebt?.totalCustomers || 0}
                </div>
                <div className="stat-label">Tổng khách hàng</div>
              </div>
              <div className="stat">
                <div className="stat-value">
                  {reports.customerDebt?.totalDebt ? formatCurrency(reports.customerDebt.totalDebt) : 'N/A'}
                </div>
                <div className="stat-label">Tổng công nợ</div>
              </div>
            </div>
            <div className="report-chart">
              <div className="chart-placeholder">
                <Users size={48} />
                <p>Biểu đồ khách hàng</p>
              </div>
            </div>
          </div>
        </div>

        <div className="report-card">
          <div className="report-header">
            <h3>Báo cáo hiệu suất đại lý</h3>
            <button 
              className="btn btn-outline btn-sm"
              onClick={() => handleExportReport('hiệu suất đại lý')}
            >
              <Download size={16} />
              Xuất Excel
            </button>
          </div>
          <div className="report-content">
            <div className="report-stats">
              <div className="stat">
                <div className="stat-value">
                  {reports.dealerPerformance?.totalDealers || 0}
                </div>
                <div className="stat-label">Tổng đại lý</div>
              </div>
              <div className="stat">
                <div className="stat-value">
                  {reports.dealerPerformance?.averagePerformance || 0}%
                </div>
                <div className="stat-label">Hiệu suất TB</div>
              </div>
            </div>
            <div className="report-chart">
              <div className="chart-placeholder">
                <TrendingUp size={48} />
                <p>Biểu đồ hiệu suất</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReportManagement;
