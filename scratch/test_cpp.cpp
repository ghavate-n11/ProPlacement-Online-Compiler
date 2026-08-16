#include <iostream>
#include <vector>
#include <numeric>

int main() {
    std::cout << "C++ 20 is fully working with portable G++!" << std::endl;
    std::vector<int> v = {10, 20, 30, 40};
    int s = std::accumulate(v.begin(), v.end(), 0);
    std::cout << "Accumulate sum: " << s << std::endl;
    return 0;
}
