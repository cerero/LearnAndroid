#include <iostream>
#include "include/method.h"

using namespace std;

int main(int argc, char *args[]) {
  int a = 1, b = 2;
  int ret = sum(a, b);
  cout << "sum of " << a << " + " << b << " is " << ret << endl;
  return 0;
}
