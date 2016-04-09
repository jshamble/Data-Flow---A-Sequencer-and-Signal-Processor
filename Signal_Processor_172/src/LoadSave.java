
public class LoadSave {
	
	ObjectList ol;
	
	public LoadSave(ObjectList o)
	{
		ol = o;
	}
	
	public void Load(String filename)
	{
		//first, load each SP object.
		//call create object for each SP object...
		
		
		/*
		for(int i = 0; i < ol.getSize(); i++)
		{
			
		}*/
		
		//then, connect all of them with wire pairs,
		//do this by checking each object, and for
		//each outlet in each object, connect a wire to the
		//corresponding inlet on the other side.
		//that's why each object has an inlet list (<Now defunct, jsut need output-input pairs),
		//and also an (outlet, inlet) pair list.
		//OUT -> IN
		
	}
	
	//also, go though each object and make sure it's saved.
	public void Save(String filename)
	{
		//output text to a file...
		//format:
		//clink: ObjectName1 UpperLeftXcoord UpperLeftYcoord
		//output-input pairs:
		//Effects: local., chained->
		
		
	}

}
