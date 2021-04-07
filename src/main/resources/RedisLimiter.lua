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

if KEYS[1] == "acquire" then
    acquire(ARGV[1])
elseif KEYS[1] == 'init' then
    init(ARGV[1], ARGV[2])
end

function init(maxLimiterSize, refreshInterval)
    redis.call("hmset", rateLimiterKey, lastAddTime, os.time(), maxLimiterSizeKey, maxLimiterSize, refreshIntervalKey, refreshInterval)
end

function tryAcquire(args)
    assert(args > 0)
    -- 获取到当前的信息
    local bucket = redis.call("hmget", rateLimiterKey, lastAddTime, maxLimiterSizeKey, remainLimiterSize)
    assert(table.maxn(bucket) > 0)
    print(bucket)
end

-- 申请 args 指定数量的令牌
function acquire(args)
    assert(args > 0)
    return tryAcquire(args)
end