import { useState, useEffect } from 'react';
import { getAllDriverStats } from '../services/driverStatsService';

/**
 * Custom hook to fetch all driver stats
 * Returns { data, loading, error, refetch }
 */
export const useFetchDrivers = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getAllDriverStats();
      
      // Handle both direct array and ApiResponse wrapper
      const drivers = response.data || response;
      setData(Array.isArray(drivers) ? drivers : []);
    } catch (err) {
      setError(err.message || 'Failed to fetch drivers');
      setData([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  return {
    data,
    loading,
    error,
    refetch: fetchData,
  };
};

export default useFetchDrivers;
