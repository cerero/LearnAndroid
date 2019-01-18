#include <iostream>
using namespace std;

class Human {
public:
  Human(char *name) {
    this->name = name;
  }
  virtual void say() {
    cout << "Human Say()" << endl;
  }
private:
  char *name;
  int age;
};

class Man : public Human {
public:
  Man(): Human("jack") {

  }
  virtual void say() {
    // Human::say();
    cout << "Man Say()" << endl;
  }
private:
  char *brother;
};

int main(int argc, char *argv[]) {
  Human h("Human");
  h.say();

  Man m;
  // m.say();

  Human &h2 = m;
  // h2.say(         );
  h2.Human::say();
  // Human *h3 = &m;
  // h3->say();

  return 0;
}
