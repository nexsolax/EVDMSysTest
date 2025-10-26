import React, { useState, useEffect } from 'react';
import { Car } from 'lucide-react';

const VehicleImage = ({ vehicle, className = '', size = 20 }) => {
  const [imageUrl, setImageUrl] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  useEffect(() => {
    const loadImage = async () => {
      if (!vehicle?.vehicleImages) {
        setError(true);
        return;
      }

      try {
        setLoading(true);
        setError(false);
        
        const vehicleImages = JSON.parse(vehicle.vehicleImages || '{}');
        const mainImageUrl = vehicleImages.main;
        
        if (!mainImageUrl) {
          setError(true);
          return;
        }

        // Tạo URL đầy đủ
        const fullUrl = `http://localhost:8080${mainImageUrl}`;
        
        // Test image accessibility
        const response = await fetch(fullUrl, { 
          method: 'HEAD',
          mode: 'cors'
        });
        
        if (response.ok) {
          setImageUrl(fullUrl);
        } else {
          console.log('Image not accessible:', fullUrl, response.status);
          setError(true);
        }
      } catch (err) {
        console.log('Error loading image:', err);
        setError(true);
      } finally {
        setLoading(false);
      }
    };

    loadImage();
  }, [vehicle]);

  if (loading) {
    return (
      <div className="placeholder-image">
        <div className="loading-spinner">⏳</div>
        <span className="placeholder-text">Đang tải...</span>
      </div>
    );
  }

  if (error || !imageUrl) {
    return (
      <div className="placeholder-image">
        <Car className="placeholder-icon" size={size} />
        <span className="placeholder-text">
          {vehicle?.variant?.model?.brand?.brandName || 'Vehicle'}
        </span>
      </div>
    );
  }

  return (
    <img 
      src={imageUrl} 
      alt={vehicle?.variant?.variantName || 'Vehicle'}
      className={className}
      onError={() => setError(true)}
      onLoad={() => console.log('Image loaded successfully:', imageUrl)}
    />
  );
};

export default VehicleImage;
