import React, { useState } from 'react';
import { BarChart3, PieChart, TrendingUp, Activity, Zap, DollarSign } from 'lucide-react';
import './ComparisonChart.css';

const ComparisonChart = ({ comparisonData, vehicles }) => {
  const [activeChart, setActiveChart] = useState('overall');

  const chartTypes = [
    { id: 'overall', label: 'Tổng điểm', icon: <BarChart3 size={16} /> },
    { id: 'price', label: 'Điểm giá', icon: <DollarSign size={16} /> },
    { id: 'performance', label: 'Hiệu suất', icon: <Zap size={16} /> },
    { id: 'range', label: 'Tầm hoạt động', icon: <Activity size={16} /> },
    { id: 'power', label: 'Công suất', icon: <TrendingUp size={16} /> }
  ];

  const getChartData = (type) => {
    return vehicles.map(vehicle => ({
      name: vehicle.variantName,
      value: vehicle[type] || 0,
      color: getVehicleColor(vehicle.rank)
    }));
  };

  const getVehicleColor = (rank) => {
    const colors = [
      '#3b82f6', // Blue
      '#10b981', // Green
      '#f59e0b', // Yellow
      '#ef4444', // Red
      '#8b5cf6', // Purple
      '#06b6d4', // Cyan
      '#84cc16', // Lime
      '#f97316'  // Orange
    ];
    return colors[(rank - 1) % colors.length] || '#6b7280';
  };

  const getMaxValue = (data) => {
    return Math.max(...data.map(item => item.value), 100);
  };

  const formatValue = (value, type) => {
    if (type === 'range') return `${value}km`;
    if (type === 'power') return `${value}kW`;
    return `${value}/100`;
  };

  const renderBarChart = (data, type) => {
    const maxValue = getMaxValue(data);
    
    return (
      <div className="bar-chart">
        {data.map((item, index) => (
          <div key={index} className="bar-item">
            <div className="bar-label">
              <span className="vehicle-name">{item.name}</span>
              <span className="vehicle-value">{formatValue(item.value, type)}</span>
            </div>
            <div className="bar-container">
              <div 
                className="bar-fill"
                style={{
                  width: `${(item.value / maxValue) * 100}%`,
                  backgroundColor: item.color
                }}
              />
            </div>
          </div>
        ))}
      </div>
    );
  };

  const renderRadarChart = () => {
    const criteria = [
      { key: 'priceScore', label: 'Giá', icon: <DollarSign size={14} /> },
      { key: 'performanceScore', label: 'Hiệu suất', icon: <Zap size={14} /> },
      { key: 'rangeScore', label: 'Tầm hoạt động', icon: <Activity size={14} /> },
      { key: 'powerScore', label: 'Công suất', icon: <TrendingUp size={14} /> },
      { key: 'batteryScore', label: 'Pin', icon: <span>🔋</span> }
    ];

    return (
      <div className="radar-chart">
        <div className="radar-grid">
          {[20, 40, 60, 80, 100].map(level => (
            <div key={level} className="radar-level">
              <div className="radar-circle" style={{ 
                width: `${level * 2}px`, 
                height: `${level * 2}px` 
              }} />
              <span className="radar-label">{level}</span>
            </div>
          ))}
        </div>
        
        {vehicles.map((vehicle, vehicleIndex) => (
          <div key={vehicle.variantId} className="radar-vehicle">
            <div 
              className="radar-polygon"
              style={{
                '--color': getVehicleColor(vehicle.rank),
                '--opacity': 0.3 + (vehicleIndex * 0.1)
              }}
            >
              {criteria.map((criterion, index) => {
                const value = vehicle[criterion.key] || 0;
                const angle = (index * 360) / criteria.length;
                const radius = (value / 100) * 50;
                const x = 50 + radius * Math.cos((angle - 90) * Math.PI / 180);
                const y = 50 + radius * Math.sin((angle - 90) * Math.PI / 180);
                
                return (
                  <div
                    key={criterion.key}
                    className="radar-point"
                    style={{
                      left: `${x}%`,
                      top: `${y}%`,
                      backgroundColor: getVehicleColor(vehicle.rank)
                    }}
                  />
                );
              })}
            </div>
          </div>
        ))}
        
        <div className="radar-criteria">
          {criteria.map((criterion, index) => {
            const angle = (index * 360) / criteria.length;
            const x = 50 + 60 * Math.cos((angle - 90) * Math.PI / 180);
            const y = 50 + 60 * Math.sin((angle - 90) * Math.PI / 180);
            
            return (
              <div
                key={criterion.key}
                className="criterion-label"
                style={{
                  left: `${x}%`,
                  top: `${y}%`,
                  transform: 'translate(-50%, -50%)'
                }}
              >
                <span className="criterion-icon">{criterion.icon}</span>
                <span className="criterion-text">{criterion.label}</span>
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  const renderPieChart = () => {
    const totalScore = vehicles.reduce((sum, vehicle) => sum + vehicle.overallScore, 0);
    
    return (
      <div className="pie-chart">
        <div className="pie-container">
          {vehicles.map((vehicle, index) => {
            const percentage = (vehicle.overallScore / totalScore) * 100;
            const startAngle = vehicles.slice(0, index).reduce((sum, v) => sum + (v.overallScore / totalScore) * 360, 0);
            const endAngle = startAngle + (vehicle.overallScore / totalScore) * 360;
            
            return (
              <div
                key={vehicle.variantId}
                className="pie-segment"
                style={{
                  '--start-angle': `${startAngle}deg`,
                  '--end-angle': `${endAngle}deg`,
                  '--color': getVehicleColor(vehicle.rank)
                }}
              />
            );
          })}
        </div>
        <div className="pie-legend">
          {vehicles.map((vehicle, index) => (
            <div key={vehicle.variantId} className="legend-item">
              <div 
                className="legend-color"
                style={{ backgroundColor: getVehicleColor(vehicle.rank) }}
              />
              <span className="legend-label">{vehicle.variantName}</span>
              <span className="legend-value">{vehicle.overallScore} điểm</span>
            </div>
          ))}
        </div>
      </div>
    );
  };

  const renderChart = () => {
    const data = getChartData(activeChart);
    
    switch (activeChart) {
      case 'overall':
        return renderBarChart(data, 'overall');
      case 'price':
        return renderBarChart(data, 'price');
      case 'performance':
        return renderBarChart(data, 'performance');
      case 'range':
        return renderBarChart(data, 'range');
      case 'power':
        return renderBarChart(data, 'power');
      default:
        return renderBarChart(data, 'overall');
    }
  };

  return (
    <div className="comparison-chart-container">
      <div className="chart-header">
        <h3>Biểu đồ so sánh</h3>
        <p>Trực quan hóa dữ liệu so sánh xe</p>
      </div>

      <div className="chart-controls">
        <div className="chart-tabs">
          {chartTypes.map(type => (
            <button
              key={type.id}
              className={`chart-tab ${activeChart === type.id ? 'active' : ''}`}
              onClick={() => setActiveChart(type.id)}
            >
              {type.icon}
              <span>{type.label}</span>
            </button>
          ))}
        </div>
      </div>

      <div className="chart-content">
        {renderChart()}
      </div>

      {activeChart === 'overall' && (
        <div className="chart-summary">
          <h4>Thống kê tổng quan</h4>
          <div className="summary-stats">
            <div className="stat-item">
              <span className="stat-label">Điểm trung bình:</span>
              <span className="stat-value">
                {Math.round(vehicles.reduce((sum, v) => sum + v.overallScore, 0) / vehicles.length)}
              </span>
            </div>
            <div className="stat-item">
              <span className="stat-label">Điểm cao nhất:</span>
              <span className="stat-value">
                {Math.max(...vehicles.map(v => v.overallScore))}
              </span>
            </div>
            <div className="stat-item">
              <span className="stat-label">Điểm thấp nhất:</span>
              <span className="stat-value">
                {Math.min(...vehicles.map(v => v.overallScore))}
              </span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ComparisonChart;
