package chap16;

class Animal {

	public void eat() {
		System.out.println("Animal eating");
	}
}

class Cat extends Animal {

	public void eat() {
		System.out.println("Cat eating");
	}

}

class Dog extends Animal {

	public void eat() {
		System.out.println("Dog eating");
	}
}

public class AnimalFeeder {
	public void feed(List<Animal> animals) {
		animals.add(new Cat());
		animals.forEach(animal -> {
			animal.eat();
		});
	}

	public static void main(String args[]) {
		List<Animal> a = new ArrayList<Animal>();
		a.add(new Cat());
		a.add(new Dog());

		new AnimalFeeder().feed(a);
		/*
		 * List<Dog> dogs = new ArrayList<>(); dogs.add(new Dog()); dogs.add(new Dog());
		 * new AnimalFeeder().feed(dogs); // not allowed
		 */

	}
}