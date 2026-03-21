import benchmark.PerfThroughput;

void main() throws InterruptedException, IOException
{
    CacheServer server = new CacheServer();
    server.start(6379);
}