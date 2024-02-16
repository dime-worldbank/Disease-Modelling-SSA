package uk.ac.ucl.protecs.sim;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.ucl.protecs.objects.Person;

public interface SpuriousSymptoms {
	public static List<Person>  filterForEligiblePeople(WorldBankCovid19Sim world, int time){return null;};
	public static List<Person>  filterForSymptomsToRemove(WorldBankCovid19Sim world, int time){return null;};
	public static void giveSymptoms(WorldBankCovid19Sim world, int time) {};
	public static void removeSymptoms(WorldBankCovid19Sim world, int time) {};
	public static void setSymptomsInPerson(Person p, int time) {};
	public static void removeSymptomsInPerson(Person p) {};
	public static <E> List<E> pickRandom(WorldBankCovid19Sim world, List<E> list, int n) {
	    return (List<E>)(world.random).ints(n, 0, list.size()).mapToObj(list::get).collect(Collectors.toList());
	  }
}