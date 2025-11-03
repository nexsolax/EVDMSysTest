import React, { useState, useEffect } from 'react';
import { RefreshCw, CheckCircle, AlertCircle, Info, BarChart3 } from 'lucide-react';
import { inventoryAPI } from '../services/api';

const InventoryStatusManager = ({ onStatusUpdate }) => {
  const [loading, setLoading] = useState(false);
  const [statusSummary, setStatusSummary] = useState(null);
  const [statusOptions, setStatusOptions] = useState({});
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    loadStatusData();
  }, []);

  const loadStatusData = async () => {
    try {
      setLoading(true);
      setError('');

      const [summaryRes, optionsRes] = await Promise.all([
        inventoryAPI.getStatusSummary(),
        inventoryAPI.getStatusOptions()
      ]);

      setStatusSummary(summaryRes.data);
      setStatusOptions(optionsRes.data);
    } catch (error) {
      console.error('Error loading status data:', error);
      setError('Không thể tải thông tin trạng thái');
    } finally {
      setLoading(false);
    }
  };

  const handleNormalizeStatuses = async () => {
    if (!window.confirm('Bạn có chắc muốn chuẩn hóa tất cả trạng thái trong hệ thống?')) return;

    try {
      setLoading(true);
      const response = await inventoryAPI.normalizeAllStatuses();
      
      if (response.data.success) {
        setSuccess(`Chuẩn hóa thành công ${response.data.updatedCount} bản ghi`);
        loadStatusData(); // Reload data
        if (onStatusUpdate) onStatusUpdate();
      }
    } catch (error) {
      console.error('Error normalizing statuses:', error);
      setError('Không thể chuẩn hóa trạng thái');
    } finally {
      setLoading(false);
    }
  };

  const validateStatus = async (status) => {
    try {
      const response = await inventoryAPI.validateStatus(status);
      return response.data.isValid;
    } catch (error) {
      console.error('Error validating status:', error);
      return false;
    }
  };

  if (loading && !statusSummary) {
    return (
      <div className="inventory-status-manager">
        <div className="loading-state">
          <div className="loading-spinner"></div>
          <p>Đang tải thông tin trạng thái...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="inventory-status-manager">
      <div className="status-manager-header">
        <h3>
          <BarChart3 className="header-icon" />
          Quản lý trạng thái kho
        </h3>
        <button 
          onClick={loadStatusData} 
          className="btn btn-secondary btn-sm"
          disabled={loading}
        >
          <RefreshCw size={16} className={loading ? 'spinning' : ''} />
          Làm mới
        </button>
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle className="alert-icon" />
          {error}
        </div>
      )}

      {success && (
        <div className="alert alert-success">
          <CheckCircle className="alert-icon" />
          {success}
        </div>
      )}

      {statusSummary && (
        <div className="status-summary">
          <div className="summary-card">
            <div className="summary-title">
              <Info className="summary-icon" />
              Tổng quan trạng thái
            </div>
            <div className="summary-content">
              <div className="summary-item">
                <span className="summary-label">Tổng số xe:</span>
                <span className="summary-value">{statusSummary.totalVehicles}</span>
              </div>
              <div className="summary-item">
                <span className="summary-label">Số trạng thái:</span>
                <span className="summary-value">{statusSummary.availableStatuses?.length || 0}</span>
              </div>
            </div>
          </div>

          <div className="status-breakdown">
            <h4>Phân bố theo trạng thái</h4>
            <div className="status-list">
              {statusSummary.statusCounts && Object.entries(statusSummary.statusCounts).map(([status, count]) => (
                <div key={status} className="status-item">
                  <div className="status-info">
                    <span className="status-name">{status}</span>
                    <span className="status-count">{count} xe</span>
                  </div>
                  <div className="status-bar">
                    <div 
                      className="status-fill" 
                      style={{ 
                        width: `${(count / statusSummary.totalVehicles) * 100}%` 
                      }}
                    ></div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {statusOptions && Object.keys(statusOptions).length > 0 && (
        <div className="status-options">
          <h4>Trạng thái hợp lệ</h4>
          <div className="options-grid">
            {Object.entries(statusOptions).map(([status, description]) => (
              <div key={status} className="option-item">
                <div className="option-status">{status}</div>
                <div className="option-description">{description}</div>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="status-actions">
        <button 
          onClick={handleNormalizeStatuses}
          className="btn btn-warning"
          disabled={loading}
        >
          <RefreshCw size={16} />
          Chuẩn hóa trạng thái
        </button>
        <button 
          onClick={loadStatusData}
          className="btn btn-primary"
          disabled={loading}
        >
          <RefreshCw size={16} />
          Cập nhật dữ liệu
        </button>
      </div>
    </div>
  );
};

export default InventoryStatusManager;

