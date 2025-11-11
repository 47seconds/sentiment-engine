import { Box, Card, CardContent, Grid, Skeleton, Stack } from '@mui/material';

// Dashboard loading skeleton
export const DashboardSkeleton = () => (
  <Box>
    {/* Header */}
    <Skeleton variant="text" width={200} height={40} sx={{ mb: 3 }} />

    {/* KPI Cards */}
    <Grid container spacing={3} sx={{ mb: 4 }}>
      {[1, 2, 3, 4].map((i) => (
        <Grid item xs={12} sm={6} md={3} key={i}>
          <Card>
            <CardContent>
              <Skeleton variant="text" width={100} />
              <Skeleton variant="text" width={80} height={40} />
              <Skeleton variant="text" width={120} />
            </CardContent>
          </Card>
        </Grid>
      ))}
    </Grid>

    {/* Charts */}
    <Grid container spacing={3}>
      <Grid item xs={12} md={4}>
        <Card>
          <CardContent>
            <Skeleton variant="text" width={150} />
            <Skeleton variant="circular" width={200} height={200} sx={{ mx: 'auto', my: 2 }} />
          </CardContent>
        </Card>
      </Grid>
      <Grid item xs={12} md={8}>
        <Card>
          <CardContent>
            <Skeleton variant="text" width={180} />
            <Skeleton variant="rectangular" height={250} sx={{ mt: 2 }} />
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  </Box>
);

// Table loading skeleton
export const TableSkeleton = ({ rows = 5, columns = 5 }) => (
  <Card>
    <CardContent>
      <Stack spacing={2}>
        {/* Search bar */}
        <Skeleton variant="rectangular" height={56} />
        
        {/* Table header */}
        <Box sx={{ display: 'flex', gap: 2 }}>
          {[...Array(columns)].map((_, i) => (
            <Skeleton key={i} variant="text" width={`${100 / columns}%`} />
          ))}
        </Box>
        
        {/* Table rows */}
        {[...Array(rows)].map((_, i) => (
          <Box key={i} sx={{ display: 'flex', gap: 2 }}>
            {[...Array(columns)].map((_, j) => (
              <Skeleton key={j} variant="text" width={`${100 / columns}%`} height={40} />
            ))}
          </Box>
        ))}
      </Stack>
    </CardContent>
  </Card>
);

// Form loading skeleton
export const FormSkeleton = () => (
  <Card>
    <CardContent>
      <Stack spacing={3}>
        <Skeleton variant="text" width={200} height={40} />
        <Skeleton variant="rectangular" height={56} />
        <Skeleton variant="rectangular" height={56} />
        <Skeleton variant="rectangular" height={120} />
        <Skeleton variant="rectangular" height={56} />
        <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
          <Skeleton variant="rectangular" width={100} height={36} />
          <Skeleton variant="rectangular" width={100} height={36} />
        </Box>
      </Stack>
    </CardContent>
  </Card>
);

// Card grid loading skeleton
export const CardGridSkeleton = ({ items = 6 }) => (
  <Grid container spacing={3}>
    {[...Array(items)].map((_, i) => (
      <Grid item xs={12} sm={6} md={4} key={i}>
        <Card>
          <CardContent>
            <Skeleton variant="text" width="60%" />
            <Skeleton variant="text" width="40%" height={30} />
            <Skeleton variant="text" width="80%" />
            <Box sx={{ display: 'flex', gap: 1, mt: 2 }}>
              <Skeleton variant="rectangular" width={60} height={24} />
              <Skeleton variant="rectangular" width={60} height={24} />
            </Box>
          </CardContent>
        </Card>
      </Grid>
    ))}
  </Grid>
);

// Profile loading skeleton
export const ProfileSkeleton = () => (
  <Grid container spacing={3}>
    <Grid item xs={12} md={4}>
      <Card>
        <CardContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', py: 3 }}>
            <Skeleton variant="circular" width={120} height={120} sx={{ mb: 2 }} />
            <Skeleton variant="text" width={150} height={30} />
            <Skeleton variant="text" width={100} />
            <Box sx={{ width: '100%', mt: 3 }}>
              <Stack spacing={1.5}>
                <Skeleton variant="text" width="100%" />
                <Skeleton variant="text" width="100%" />
                <Skeleton variant="text" width="100%" />
              </Stack>
            </Box>
          </Box>
        </CardContent>
      </Card>
    </Grid>
    <Grid item xs={12} md={8}>
      <Card>
        <CardContent>
          <Skeleton variant="text" width={200} height={30} sx={{ mb: 3 }} />
          <Grid container spacing={3}>
            {[...Array(6)].map((_, i) => (
              <Grid item xs={12} sm={6} key={i}>
                <Skeleton variant="rectangular" height={56} />
              </Grid>
            ))}
          </Grid>
        </CardContent>
      </Card>
    </Grid>
  </Grid>
);

// Settings loading skeleton
export const SettingsSkeleton = () => (
  <Grid container spacing={3}>
    {[1, 2, 3, 4].map((i) => (
      <Grid item xs={12} md={6} key={i}>
        <Card>
          <CardContent>
            <Skeleton variant="text" width={150} height={30} sx={{ mb: 3 }} />
            <Stack spacing={2}>
              <Skeleton variant="rectangular" height={56} />
              <Skeleton variant="rectangular" height={56} />
              <Skeleton variant="rectangular" height={56} />
            </Stack>
          </CardContent>
        </Card>
      </Grid>
    ))}
  </Grid>
);
