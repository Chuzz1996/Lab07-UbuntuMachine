local current = redis.call("HGET",KEYS[1],"currentWord")
local word = redis.call("HGET", KEYS[1],"word")

for i = 1, #word do
    local c = word:sub(i,i)
    if c == ARGV[1]
	then
        current=current:sub(1, i-1)..c..current:sub(i+1)
    end
end

redis.call("HSET", KEYS[1],"currentWord", current)

return "finalizado"
