-- 令牌桶的 key
local rateLimiterKey = "reteLimiter"
-- 最后一次添加令牌的时间
local lastAddTime = "lastAddTime"
-- 最大的令牌的数量
local maxLimiterSizeKey = "maxLimiterSize"
-- 剩余的令牌的数量
local remainLimiterSize = "remainLimiterSize"
-- 刷新时间
local refreshIntervalKey = "refreshInterval"

local function init(now, maxLimiterSize, refreshInterval)
    redis.call("hmset", "reteLimiter", lastAddTime, tonumber(now), maxLimiterSizeKey, tonumber(maxLimiterSize), refreshIntervalKey, tonumber(refreshInterval), remainLimiterSize, tonumber(0))
end

local function tryAcquire(args)
    -- assert(args > 0)
    -- 获取到当前的信息
    local bucket = redis.call("hmget", rateLimiterKey, lastAddTime, maxLimiterSizeKey, remainLimiterSize)
    assert(table.maxn(bucket) > 0)
    print(bucket)
end

-- 申请 args 指定数量的令牌
local function acquire(args)
    -- assert(tonumber(args) > 0)
    return tryAcquire(args)
end

if KEYS[1] == "acquire" then
    acquire(ARGV[1])
elseif KEYS[1] == 'init' then
    init(ARGV[1], ARGV[2], ARGV[3])
end