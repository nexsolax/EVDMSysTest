import React, { useState, useEffect } from 'react';
import { Car } from 'lucide-react';

const VehicleImage = ({ vehicle, className = '', size = 20 }) => {
  const [imageUrl, setImageUrl] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  useEffect(() => {
    const loadImage = async () => {
      let imagePath = null;
      const baseUrl = process.env.REACT_APP_API_URL 
        ? process.env.REACT_APP_API_URL.replace('/api', '') 
        : 'http://localhost:8080';

      console.log('VehicleImage - vehicle data:', vehicle);
      console.log('VehicleImage - vehicle.vehicleImages:', vehicle?.vehicleImages);
      console.log('VehicleImage - vehicle.variant:', vehicle?.variant);
      console.log('VehicleImage - vehicle.variant?.variantImageUrl:', vehicle?.variant?.variantImageUrl);
      console.log('VehicleImage - vehicle.variant?.variantImagePath:', vehicle?.variant?.variantImagePath);

      // Priority 1: Check vehicle.vehicleImages (JSON string)
      if (vehicle?.vehicleImages) {
        try {
          const vehicleImages = JSON.parse(vehicle.vehicleImages || '{}');
          imagePath = vehicleImages.main;
          console.log('VehicleImage - Found in vehicleImages.main:', imagePath);
        } catch (err) {
          console.log('Error parsing vehicleImages:', err);
        }
      }

      // Priority 2: Check vehicle.variant.variantImageUrl
      if (!imagePath && vehicle?.variant?.variantImageUrl) {
        imagePath = vehicle.variant.variantImageUrl;
        console.log('VehicleImage - Found in variant.variantImageUrl:', imagePath);
      }

      // Priority 3: Check vehicle.variant.variantImagePath
      if (!imagePath && vehicle?.variant?.variantImagePath) {
        imagePath = vehicle.variant.variantImagePath;
        console.log('VehicleImage - Found in variant.variantImagePath:', imagePath);
      }

      if (!imagePath) {
        console.log('VehicleImage - No image path found, showing placeholder');
        setError(true);
        return;
      }

      try {
        setLoading(true);
        setError(false);
        
        // Build full URL
        let fullUrl;
        if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
          fullUrl = imagePath;
        } else {
          const cleanPath = imagePath.startsWith('/') ? imagePath : `/${imagePath}`;
          fullUrl = `${baseUrl}${cleanPath}`;
        }
        
        console.log('VehicleImage - Final image URL:', fullUrl);
        
        // Set image URL directly - let browser handle loading and errors
        setImageUrl(fullUrl);
      } catch (err) {
        console.log('Error building image URL:', err);
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
