import React, { useState } from 'react';
import { BarChart3, TrendingUp, Activity, Zap, DollarSign } from 'lucide-react';
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
