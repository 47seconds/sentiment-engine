# OpenRouter Sentiment Analysis Test Script
Write-Host "=== OPENROUTER SENTIMENT ANALYSIS TEST ===" -ForegroundColor Yellow

# Test configuration
$baseUrl = "http://localhost:8080/api"
$testText = "good boi, like him specially his dih"
$testRating = 4

Write-Host "`n🔧 Step 1: Testing backend connectivity..." -ForegroundColor Cyan
try {
    $healthCheck = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body '{"email":"invalid","password":"invalid"}' -ContentType "application/json" -TimeoutSec 5 -ErrorAction Stop
    Write-Host "✅ Backend is responding" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "✅ Backend is responding (401 expected for invalid credentials)" -ForegroundColor Green
    } else {
        Write-Host "❌ Backend not responding: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
}

Write-Host "`n🔑 Step 2: Login with valid credentials..." -ForegroundColor Cyan
try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body '{"email":"testuser@moveinsync.com","password":"TestPass123!"}' -ContentType "application/json" -TimeoutSec 10
    $token = $loginResponse.data.token
    Write-Host "✅ Login successful" -ForegroundColor Green
} catch {
    Write-Host "❌ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Write-Host "`n📝 Step 3: Testing problematic feedback..." -ForegroundColor Cyan
Write-Host "   Text: '$testText'" -ForegroundColor White
Write-Host "   Rating: $testRating/5" -ForegroundColor White

$feedbackData = @{
    driverId = 21
    feedbackText = $testText
    rating = $testRating
    feedbackType = "GENERAL_FEEDBACK"
    source = "WEB_PORTAL"
} | ConvertTo-Json

Write-Host "   Submitting feedback..." -ForegroundColor Yellow

try {
    $result = Invoke-RestMethod -Uri "$baseUrl/feedback" -Method POST -Body $feedbackData -Headers $headers -TimeoutSec 15
    $feedbackId = $result.data.id
    Write-Host "✅ Feedback submitted successfully (ID: $feedbackId)" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to submit feedback: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "`n⏳ Step 4: Waiting for OpenRouter AI processing..." -ForegroundColor Cyan
Write-Host "   (This should take 3-8 seconds if OpenRouter is working)" -ForegroundColor Gray
Start-Sleep -Seconds 10

Write-Host "`n🔍 Step 5: Retrieving processed feedback..." -ForegroundColor Cyan
try {
    $processed = Invoke-RestMethod -Uri "$baseUrl/feedback/$feedbackId" -Method GET -Headers $headers -TimeoutSec 10
    
    Write-Host "`n📊 RESULTS:" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
    Write-Host "📝 Original Text:     '$testText'" -ForegroundColor White
    Write-Host "⭐ Rating:            $testRating/5" -ForegroundColor White
    Write-Host "📊 Sentiment Score:   $($processed.data.sentimentScore)" -ForegroundColor Cyan
    Write-Host "🏷️  Sentiment Label:   $($processed.data.sentimentLabel)" -ForegroundColor Magenta
    Write-Host "📋 Feedback Type:     $($processed.data.feedbackType)" -ForegroundColor Blue
    Write-Host "🎯 Requires Attention: $($processed.data.requiresAttention)" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
    
    # Analysis
    Write-Host "`n🧠 ANALYSIS:" -ForegroundColor Yellow
    
    # Check sentiment correctness
    if ($processed.data.sentimentLabel -match "POSITIVE") {
        Write-Host "✅ SENTIMENT: Correctly identified as POSITIVE" -ForegroundColor Green
        Write-Host "   OpenRouter AI is working properly!" -ForegroundColor Green
    } elseif ($processed.data.sentimentLabel -match "NEUTRAL") {
        Write-Host "⚠️  SENTIMENT: Identified as NEUTRAL (could be acceptable)" -ForegroundColor Yellow
        Write-Host "   OpenRouter might be working but being conservative" -ForegroundColor Yellow
    } else {
        Write-Host "❌ SENTIMENT: Incorrectly identified as $($processed.data.sentimentLabel)" -ForegroundColor Red
        Write-Host "   Expected POSITIVE for text containing 'good', 'like him'" -ForegroundColor Red
    }
    
    # Check feedback type
    if ($processed.data.feedbackType -match "PRAISE|POSITIVE") {
        Write-Host "✅ TYPE: Correctly classified as positive feedback type" -ForegroundColor Green
    } elseif ($processed.data.feedbackType -eq "GENERAL_FEEDBACK") {
        Write-Host "⚠️  TYPE: Kept as GENERAL_FEEDBACK (neutral classification)" -ForegroundColor Yellow
    } else {
        Write-Host "❌ TYPE: Incorrectly classified as $($processed.data.feedbackType)" -ForegroundColor Red
        Write-Host "   Should not be COMPLAINT for positive text" -ForegroundColor Red
    }
    
    # Check score range
    $score = [double]$processed.data.sentimentScore
    if ($score -gt 0.2) {
        Write-Host "✅ SCORE: Positive score ($score) matches positive sentiment" -ForegroundColor Green
    } elseif ($score -ge -0.2 -and $score -le 0.2) {
        Write-Host "⚠️  SCORE: Neutral score ($score) - could be acceptable" -ForegroundColor Yellow
    } else {
        Write-Host "❌ SCORE: Negative score ($score) for positive text" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Failed to retrieve feedback: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "`n🎯 CONCLUSION:" -ForegroundColor Yellow
if ($processed.data.sentimentLabel -match "POSITIVE" -and $processed.data.sentimentScore -gt 0) {
    Write-Host "🎉 SUCCESS: OpenRouter AI sentiment analysis is working correctly!" -ForegroundColor Green
    Write-Host "   The problematic feedback is now properly classified as positive." -ForegroundColor Green
} else {
    Write-Host "⚠️  ISSUE: There may still be problems with the OpenRouter integration." -ForegroundColor Yellow
    Write-Host "   Check the backend logs in the other terminal for OpenRouter API calls." -ForegroundColor Yellow
}

Write-Host "`n✅ Test completed!" -ForegroundColor Green