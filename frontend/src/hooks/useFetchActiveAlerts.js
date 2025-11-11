import { useState, useEffect } from 'react';
import { getActiveAlerts } from '../services/alertService';

/**
 * Custom hook to fetch active alerts
 * Returns { alerts, loading, error, refetch }
 */
export const useFetchActiveAlerts = () => {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getActiveAlerts();
      
      // Handle both direct array and ApiResponse wrapper
      const data = response.data || response;
      setAlerts(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'Failed to fetch alerts');
      setAlerts([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  return {
    alerts,
    loading,
    error,
    refetch: fetchData,
  };
};

export default useFetchActiveAlerts;
