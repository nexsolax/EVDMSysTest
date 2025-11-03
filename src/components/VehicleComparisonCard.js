import React from 'react';
import { Car, Battery, Gauge, Zap, Clock, DollarSign, Star, CheckCircle, XCircle } from 'lucide-react';
import './VehicleComparisonCard.css';

const VehicleComparisonCard = ({ 
  vehicle, 
  isSelected = false, 
  onSelect, 
  onRemove, 
  showRemoveButton = false,
  comparisonData = null,
  rank = null 
}) => {
  const formatPrice = (price) => {
    if (!price) return 'Liên hệ';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      minimumFractionDigits: 0,
    }).format(price);
  };

  const getRankIcon = (rank) => {
    if (rank === 1) return <Star className="rank-icon gold" />;
    if (rank === 2) return <Star className="rank-icon silver" />;
    if (rank === 3) return <Star className="rank-icon bronze" />;
    return <span className="rank-number">{rank}</span>;
  };

  const getScoreColor = (score) => {
    if (score >= 80) return 'excellent';
    if (score >= 60) return 'good';
    if (score >= 40) return 'average';
    return 'poor';
  };

  return (
    <div className={`vehicle-comparison-card ${isSelected ? 'selected' : ''} ${comparisonData ? 'comparison-mode' : ''}`}>
      {rank && (
        <div className="rank-badge">
          {getRankIcon(rank)}
        </div>
      )}
      
      {showRemoveButton && (
        <button 
          className="remove-button"
          onClick={() => onRemove(vehicle.variantId)}
          title="Xóa khỏi so sánh"
        >
          <XCircle size={20} />
        </button>
      )}

      <div className="vehicle-image">
        {vehicle.imageUrl ? (
          <img 
            src={vehicle.imageUrl} 
            alt={vehicle.variantName}
            onError={(e) => {
              e.target.style.display = 'none';
              e.target.nextSibling.style.display = 'flex';
            }}
          />
        ) : null}
        <div 
          className="vehicle-placeholder"
          style={{ display: vehicle.imageUrl ? 'none' : 'flex' }}
        >
          <Car size={48} />
        </div>
      </div>

      <div className="vehicle-info">
        <div className="vehicle-header">
          <h3 className="vehicle-name">{vehicle.variantName}</h3>
          <p className="vehicle-brand-model">
            {vehicle.model?.brand?.brandName} {vehicle.model?.modelName}
          </p>
        </div>

        {comparisonData && (
          <div className="comparison-score">
            <div className={`score-circle ${getScoreColor(comparisonData.overallScore)}`}>
              <span className="score-value">{comparisonData.overallScore}</span>
              <span className="score-label">điểm</span>
            </div>
          </div>
        )}

        <div className="vehicle-specs">
          <div className="spec-item">
            <DollarSign size={16} />
            <span className="spec-label">Giá:</span>
            <span className="spec-value">{formatPrice(vehicle.priceBase || vehicle.basePrice)}</span>
          </div>
          
          <div className="spec-item">
            <Battery size={16} />
            <span className="spec-label">Pin:</span>
            <span className="spec-value">{vehicle.batteryCapacity || 'N/A'}kWh</span>
          </div>
          
          <div className="spec-item">
            <Gauge size={16} />
            <span className="spec-label">Tầm hoạt động:</span>
            <span className="spec-value">{vehicle.rangeKm || vehicle.range || 'N/A'}km</span>
          </div>
          
          <div className="spec-item">
            <Zap size={16} />
            <span className="spec-label">Công suất:</span>
            <span className="spec-value">{vehicle.powerKw || 'N/A'}kW</span>
          </div>
          
          <div className="spec-item">
            <Clock size={16} />
            <span className="spec-label">Sạc nhanh:</span>
            <span className="spec-value">
              {vehicle.chargingTimeFast ? `${vehicle.chargingTimeFast} phút` : 
               vehicle.chargingTimeSlow ? `${vehicle.chargingTimeSlow} phút (chậm)` : 'N/A'}
            </span>
          </div>
        </div>

        {comparisonData && (
          <div className="comparison-details">
            <div className="detail-item">
              <span className="detail-label">Xếp hạng:</span>
              <span className="detail-value">#{comparisonData.rank}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Điểm giá:</span>
              <span className="detail-value">{comparisonData.priceScore}/100</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Điểm hiệu suất:</span>
              <span className="detail-value">{comparisonData.performanceScore}/100</span>
            </div>
          </div>
        )}

        <div className="vehicle-availability">
          {vehicle.isActive ? (
            <div className="availability available">
              <CheckCircle size={16} />
              <span>Có sẵn</span>
            </div>
          ) : (
            <div className="availability unavailable">
              <XCircle size={16} />
              <span>Không có sẵn</span>
            </div>
          )}
        </div>

        {!comparisonData && (
          <div className="card-actions">
            <button 
              className={`select-button ${isSelected ? 'selected' : ''}`}
              onClick={() => onSelect(vehicle.variantId)}
            >
              {isSelected ? 'Đã chọn' : 'Chọn để so sánh'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default VehicleComparisonCard;
