import { useState, useEffect } from 'react';
import { getOverallStats } from '../services/driverStatsService';

/**
 * Custom hook to fetch overall statistics (for dashboard KPIs)
 * Returns { stats, loading, error, refetch }
 */
export const useFetchOverallStats = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getOverallStats();
      
      // Handle both direct object and ApiResponse wrapper
      const data = response.data || response;
      setStats(data);
    } catch (err) {
      setError(err.message || 'Failed to fetch statistics');
      setStats(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  return {
    stats,
    loading,
    error,
    refetch: fetchData,
  };
};

export default useFetchOverallStats;
