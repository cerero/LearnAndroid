#include <stdio.h>
#include "method.h"

int main(int argc, char *args[]) {
  int a = 1, b = 2;
  int ret = sum(a, b);
  printf("sum of %d and %d is %d\n", a, b, ret);
  return 0;
}
