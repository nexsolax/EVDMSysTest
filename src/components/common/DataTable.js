import React from 'react';
import { Edit, Trash2, Eye, CheckCircle, PenTool } from 'lucide-react';
import './DataTable.css';

const DataTable = ({
  data = [],
  columns = [],
  loading = false,
  emptyMessage = 'Không có dữ liệu',
  onEdit,
  onDelete,
  onView,
  onUpdateStatus,
  onSignContract,
  onActivateDeactivate,
  onSendQuotation,
  onConvertToOrder,
  onExportPDF,
  onProcessPayment,
  onRefundPayment,
  onExportReceipt,
  onScheduleDelivery,
  onCompleteDelivery,
  onGetTracking,
  onReserveVehicle,
  onReleaseVehicle,
  onGetVehicleHistory,
  onViewInventory,
  onTransferWarehouse,
  onSendContract,
  onTerminateContract,
  onConvertToContract,
  onCancelOrder,
  actions = true,
  searchable = true,
  searchTerm = '',
  onSearchChange,
  searchPlaceholder = 'Tìm kiếm...',
  className = ''
}) => {
  console.log('DataTable props:', { data, columns, loading, emptyMessage });
  const handleAction = (action, item, e) => {
    e.stopPropagation();
    if (action === 'edit' && onEdit) {
      onEdit(item);
    } else if (action === 'delete' && onDelete) {
      onDelete(item);
    } else if (action === 'view' && onView) {
      onView(item);
    } else if (action === 'updateStatus' && onUpdateStatus) {
      onUpdateStatus(item);
    } else if (action === 'signContract' && onSignContract) {
      onSignContract(item);
    } else if (action === 'activateDeactivate' && onActivateDeactivate) {
      onActivateDeactivate(item);
    } else if (action === 'sendQuotation' && onSendQuotation) {
      onSendQuotation(item);
    } else if (action === 'convertToOrder' && onConvertToOrder) {
      onConvertToOrder(item);
    } else if (action === 'exportPDF' && onExportPDF) {
      onExportPDF(item);
    } else if (action === 'processPayment' && onProcessPayment) {
      onProcessPayment(item);
    } else if (action === 'refundPayment' && onRefundPayment) {
      onRefundPayment(item);
    } else if (action === 'exportReceipt' && onExportReceipt) {
      onExportReceipt(item);
    } else if (action === 'scheduleDelivery' && onScheduleDelivery) {
      onScheduleDelivery(item);
    } else if (action === 'completeDelivery' && onCompleteDelivery) {
      onCompleteDelivery(item);
    } else if (action === 'getTracking' && onGetTracking) {
      onGetTracking(item);
    } else if (action === 'reserveVehicle' && onReserveVehicle) {
      onReserveVehicle(item);
    } else if (action === 'releaseVehicle' && onReleaseVehicle) {
      onReleaseVehicle(item);
    } else if (action === 'getVehicleHistory' && onGetVehicleHistory) {
      onGetVehicleHistory(item);
    } else if (action === 'viewInventory' && onViewInventory) {
      onViewInventory(item);
    } else if (action === 'transferWarehouse' && onTransferWarehouse) {
      onTransferWarehouse(item);
    } else if (action === 'sendContract' && onSendContract) {
      onSendContract(item);
    } else if (action === 'terminateContract' && onTerminateContract) {
      onTerminateContract(item);
    }
  };

  if (loading) {
    return (
      <div className="data-table-loading">
        <div className="loading-spinner"></div>
        <p>Đang tải dữ liệu...</p>
      </div>
    );
  }

  return (
    <div className={`data-table-container ${className}`}>
      {searchable && (
        <div className="data-table-search">
          <input
            type="text"
            placeholder={searchPlaceholder}
            value={searchTerm}
            onChange={(e) => onSearchChange && onSearchChange(e.target.value)}
            className="search-input"
          />
        </div>
      )}

      <div className="data-table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              {columns.map((column, index) => (
                <th key={index} className={column.className || ''}>
                  {column.header}
                </th>
              ))}
              {actions && <th className="actions-column">Thao tác</th>}
            </tr>
          </thead>
          <tbody>
            {data.length === 0 ? (
              <tr>
                <td colSpan={columns.length + (actions ? 1 : 0)} className="empty-cell">
                  <div className="empty-state">
                    <div className="empty-icon">📋</div>
                    <p>{emptyMessage}</p>
                    <p style={{ fontSize: '12px', color: '#666' }}>
                      Debug: data.length = {data.length}, columns.length = {columns.length}
                    </p>
                  </div>
                </td>
              </tr>
            ) : (
              data.map((item, rowIndex) => {
                console.log('Rendering row:', rowIndex, item);
                return (
                <tr key={item.id || rowIndex} className="data-row">
                  {columns.map((column, colIndex) => (
                    <td key={colIndex} className={column.cellClassName || ''}>
                      {column.render ? column.render(item) : (() => {
                        const value = item[column.key];
                        if (value === null || value === undefined) return 'N/A';
                        if (typeof value === 'object') return JSON.stringify(value);
                        return value;
                      })()}
                    </td>
                  ))}
                  {actions && (
                    <td className="actions-cell">
                      <div className="action-buttons">
                        {onView && (
                          <button
                            className="action-btn view-btn"
                            onClick={(e) => handleAction('view', item, e)}
                            title="Xem chi tiết"
                          >
                            <Eye size={18} />
                          </button>
                        )}
                        {onEdit && (
                          <button
                            className="action-btn edit-btn"
                            onClick={(e) => handleAction('edit', item, e)}
                            title="Chỉnh sửa"
                          >
                            <Edit size={18} />
                          </button>
                        )}
                        {onUpdateStatus && (
                          <button
                            className="action-btn status-btn"
                            onClick={(e) => handleAction('updateStatus', item, e)}
                            title="Cập nhật trạng thái"
                          >
                            <CheckCircle size={18} />
                          </button>
                        )}
                        {onSignContract && (
                          <button
                            className="action-btn sign-btn"
                            onClick={(e) => handleAction('signContract', item, e)}
                            title="Ký hợp đồng"
                          >
                            <PenTool size={18} />
                          </button>
                        )}
                        {onActivateDeactivate && (
                          <button
                            className="action-btn activate-btn"
                            onClick={(e) => handleAction('activateDeactivate', item, e)}
                            title={item.isActive ? "Vô hiệu hóa" : "Kích hoạt"}
                          >
                            {item.isActive ? "Vô hiệu hóa" : "Kích hoạt"}
                          </button>
                        )}
                        {onSendQuotation && (
                          <button
                            className="action-btn send-btn"
                            onClick={(e) => handleAction('sendQuotation', item, e)}
                            title="Gửi báo giá"
                          >
                            Gửi
                          </button>
                        )}
                        {onConvertToOrder && (
                          <button
                            className="action-btn convert-btn"
                            onClick={(e) => handleAction('convertToOrder', item, e)}
                            title="Chuyển thành đơn hàng"
                          >
                            Chuyển đổi
                          </button>
                        )}
                        {onExportPDF && (
                          <button
                            className="action-btn export-btn"
                            onClick={(e) => handleAction('exportPDF', item, e)}
                            title="Xuất PDF"
                          >
                            PDF
                          </button>
                        )}
                        {onConvertToContract && (
                          <button
                            className="action-btn convert-btn"
                            onClick={(e) => handleAction('convertToContract', item, e)}
                            title="Chuyển thành hợp đồng"
                          >
                            Hợp đồng
                          </button>
                        )}
                        {onCancelOrder && (
                          <button
                            className="action-btn cancel-btn"
                            onClick={(e) => handleAction('cancelOrder', item, e)}
                            title="Hủy đơn hàng"
                          >
                            Hủy
                          </button>
                        )}
                        {onSendContract && (
                          <button
                            className="action-btn send-btn"
                            onClick={(e) => handleAction('sendContract', item, e)}
                            title="Gửi hợp đồng"
                          >
                            Gửi
                          </button>
                        )}
                        {onTerminateContract && (
                          <button
                            className="action-btn terminate-btn"
                            onClick={(e) => handleAction('terminateContract', item, e)}
                            title="Chấm dứt hợp đồng"
                          >
                            Chấm dứt
                          </button>
                        )}
                        {onDelete && (
                          <button
                            className="action-btn delete-btn"
                            onClick={(e) => handleAction('delete', item, e)}
                            title="Xóa"
                          >
                            <Trash2 size={18} />
                          </button>
                        )}
                      </div>
                    </td>
                  )}
                </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default DataTable;
