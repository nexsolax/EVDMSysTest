import React from 'react';
import { TrendingUp, TrendingDown, Minus, Star, Award, Target } from 'lucide-react';
import './ComparisonTable.css';

const ComparisonTable = ({ comparisonData, vehicles }) => {
  const formatPrice = (price) => {
    if (!price) return 'Liên hệ';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      minimumFractionDigits: 0,
    }).format(price);
  };

  const formatNumber = (value, unit = '') => {
    if (value === null || value === undefined) return 'N/A';
    return `${value.toLocaleString('vi-VN')}${unit}`;
  };

  const getTrendIcon = (value, isHigherBetter = true) => {
    if (value === 'best') {
      return <TrendingUp className="trend-icon best" />;
    } else if (value === 'worst') {
      return <TrendingDown className="trend-icon worst" />;
    }
    return <Minus className="trend-icon neutral" />;
  };

  const getScoreColor = (score) => {
    if (score >= 80) return 'excellent';
    if (score >= 60) return 'good';
    if (score >= 40) return 'average';
    return 'poor';
  };

  const getComparisonRows = () => {
    const rows = [
      {
        label: 'Tổng điểm',
        key: 'overallScore',
        format: (value) => `${value}/100`,
        icon: <Award size={16} />,
        isScore: true
      },
      {
        label: 'Xếp hạng',
        key: 'rank',
        format: (value) => `#${value}`,
        icon: <Target size={16} />,
        isRank: true
      },
      {
        label: 'Giá bán',
        key: 'basePrice',
        format: formatPrice,
        icon: <span className="currency-icon">₫</span>,
        isPrice: true
      },
      {
        label: 'Tầm hoạt động',
        key: 'range',
        format: (value) => formatNumber(value, 'km'),
        icon: <span className="range-icon">⚡</span>,
        isHigherBetter: true
      },
      {
        label: 'Công suất',
        key: 'powerKw',
        format: (value) => formatNumber(value, 'kW'),
        icon: <span className="power-icon">⚡</span>,
        isHigherBetter: true
      },
      {
        label: 'Dung lượng pin',
        key: 'batteryCapacity',
        format: (value) => formatNumber(value, 'kWh'),
        icon: <span className="battery-icon">🔋</span>,
        isHigherBetter: true
      },
      {
        label: 'Thời gian sạc',
        key: 'chargingTime',
        format: (value) => formatNumber(value, 'h'),
        icon: <span className="time-icon">⏱️</span>,
        isHigherBetter: false
      },
      {
        label: 'Gia tốc 0-100km/h',
        key: 'acceleration',
        format: (value) => formatNumber(value, 's'),
        icon: <span className="speed-icon">🏃</span>,
        isHigherBetter: false
      },
      {
        label: 'Tốc độ tối đa',
        key: 'topSpeed',
        format: (value) => formatNumber(value, 'km/h'),
        icon: <span className="speed-icon">🏎️</span>,
        isHigherBetter: true
      },
      {
        label: 'Điểm giá',
        key: 'priceScore',
        format: (value) => `${value}/100`,
        icon: <span className="score-icon">💰</span>,
        isScore: true
      },
      {
        label: 'Điểm hiệu suất',
        key: 'performanceScore',
        format: (value) => `${value}/100`,
        icon: <span className="score-icon">⚡</span>,
        isScore: true
      },
      {
        label: 'Tình trạng',
        key: 'isActive',
        format: (value) => value ? 'Có sẵn' : 'Không có sẵn',
        icon: <span className="status-icon">📦</span>,
        isStatus: true
      }
    ];

    return rows;
  };

  const findBestAndWorst = (key, isHigherBetter = true) => {
    const values = vehicles.map(v => v[key]).filter(v => v !== null && v !== undefined);
    if (values.length === 0) return { best: null, worst: null };

    const sorted = [...values].sort((a, b) => isHigherBetter ? b - a : a - b);
    const best = sorted[0];
    const worst = sorted[sorted.length - 1];

    return { best, worst };
  };

  const getCellValue = (vehicle, row) => {
    const value = vehicle[row.key];
    const formatted = row.format(value);
    
    if (row.isScore || row.isRank) {
      return {
        value: formatted,
        score: vehicle[row.key],
        trend: null
      };
    }

    if (row.isPrice) {
      return {
        value: formatted,
        score: vehicle.priceScore,
        trend: null
      };
    }

    if (row.isStatus) {
      return {
        value: formatted,
        score: vehicle.isActive ? 100 : 0,
        trend: null
      };
    }

    const { best, worst } = findBestAndWorst(row.key, row.isHigherBetter);
    let trend = null;
    if (value === best && value !== worst) trend = 'best';
    else if (value === worst && value !== best) trend = 'worst';

    return {
      value: formatted,
      score: vehicle[`${row.key}Score`] || null,
      trend
    };
  };

  const rows = getComparisonRows();

  return (
    <div className="comparison-table-container">
      <div className="comparison-table-header">
        <h3>Bảng so sánh chi tiết</h3>
        <p>So sánh các thông số kỹ thuật và điểm đánh giá</p>
      </div>

      <div className="comparison-table-wrapper">
        <table className="comparison-table">
          <thead>
            <tr>
              <th className="criteria-column">Tiêu chí</th>
              {vehicles.map((vehicle, index) => (
                <th key={vehicle.variantId} className="vehicle-column">
                  <div className="vehicle-header">
                    <div className="vehicle-name">{vehicle.variantName}</div>
                    <div className="vehicle-brand">{vehicle.model?.brand?.brandName}</div>
                    {vehicle.rank && (
                      <div className="vehicle-rank">
                        <Star size={14} />
                        <span>#{vehicle.rank}</span>
                      </div>
                    )}
                  </div>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((row, rowIndex) => (
              <tr key={rowIndex} className="comparison-row">
                <td className="criteria-cell">
                  <div className="criteria-info">
                    <span className="criteria-icon">{row.icon}</span>
                    <span className="criteria-label">{row.label}</span>
                  </div>
                </td>
                {vehicles.map((vehicle, vehicleIndex) => {
                  const cellData = getCellValue(vehicle, row);
                  return (
                    <td key={vehicle.variantId} className="vehicle-cell">
                      <div className="cell-content">
                        <div className="cell-value">
                          {cellData.value}
                        </div>
                        {cellData.trend && (
                          <div className="cell-trend">
                            {getTrendIcon(cellData.trend, row.isHigherBetter)}
                          </div>
                        )}
                        {cellData.score !== null && (row.isScore || row.isRank) && (
                          <div className={`cell-score ${getScoreColor(cellData.score)}`}>
                            {cellData.score}
                          </div>
                        )}
                      </div>
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="comparison-legend">
        <div className="legend-item">
          <TrendingUp className="trend-icon best" />
          <span>Tốt nhất</span>
        </div>
        <div className="legend-item">
          <TrendingDown className="trend-icon worst" />
          <span>Kém nhất</span>
        </div>
        <div className="legend-item">
          <Minus className="trend-icon neutral" />
          <span>Trung bình</span>
        </div>
      </div>
    </div>
  );
};

export default ComparisonTable;
