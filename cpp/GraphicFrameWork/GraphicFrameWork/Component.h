#pragma once
#include <iostream>
#include <vector>
#include "AppObject.h"
#include <string>
using namespace std;
class Component;
struct ComponentData;
class ComponentManager {
public:
	ComponentManager();
	void AttachComponent(Component* component, AppObject *parent);
	vector <Component*> UpdateSectorComponents;
	vector <Component*> InputSectorComponents;
	vector <Component*> DrawSectorComponents;
};

class Component
{
public:
	void BasicInit(AppObject *parent);
	virtual void Init(AppObject *parent);
	virtual int run(vector <AppObject*> AppObjects) ;
	virtual int id();
	virtual string RunSector();
	AppObject *parent_m;
};

class TestComponent : public Component {
public:
	int run(vector <AppObject*> AppObjects);
	int id();
	string RunSector();
};

class CollisionBox : public Component {
public:
	CollisionBox(vector <AppObject*> AppObjects);
	int run(vector <AppObject*> AppObjects);
	int id();
	bool isCollided(AppObject* object);
	string RunSector();
	bool colNot0 = false;
private:
	vector <AppObject*> AppObjects_m;
};

class CollisionCircle : public Component {
public:
	CollisionCircle(vector <AppObject*> AppObjects);
	int run(vector <AppObject*> AppObjects);
	int id();
	bool isCollided(AppObject* object);
	string RunSector();
private:
	vector <AppObject*> AppObjects_m;
};

class ResolveGameCollision : public Component {
public:

private:

};
