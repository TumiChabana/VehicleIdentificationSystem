# Vehicle Identification System

A JavaFX desktop application for vehicle tracking and identification.

## Tech Stack
- JavaFX 21
- PostgreSQL (Neon cloud database)
- JDBC
- Maven
- BCrypt password hashing

## Modules
- Admin - User access control
- Workshop - Vehicle registration & service records
- Customer - Owner information & queries
- Police - Reports & violations
- Insurance - Policy management

## What This System Actually Is
Imagine the government of Lesotho builds a centralized database of every vehicle in the country. Instead of police carrying paper files, workshop owners keeping manual logs, and insurance companies making phone calls to verify coverage — everything is in one system.
One database.
Four types of users.
Everyone sees only what they need.

The Real World Story
The Characters
Litumeleng Mokoena  ← owns a Toyota Hilux LSO-001-AA
Officer Molapo      ← traffic police officer
Maseru Auto Workshop← registered vehicle workshop
LNI (insurer)       ← Lesotho National Insurance
Admin               ← government system administrator

How It Works Day by Day
Day 1 — Admin sets everything up
The government administrator logs in. He registers Litumeleng as a customer. He registers her Toyota Hilux as a vehicle. He creates login accounts:

customer1 linked to Litumeleng
officer1 for Officer Molapo
workshop1 for Maseru Auto Workshop
insurer1 for LNI


Day 2 — Litumeleng takes her car for a service
She drives to Maseru Auto Workshop. The workshop logs into the system as workshop1. They find her vehicle by registration number. They perform an oil change and record it:
Vehicle: LSO-001-AA
Service: Oil Change
Cost: M850
Date: Today
That record is now in the database permanently.

Day 3 — Litumeleng logs into her portal
She opens the app, logs in as customer1. She sees:

Her profile (name, phone, address)
Her Toyota Hilux on the My Vehicles tab
The oil change the workshop recorded — she can see the cost
Her insurance policy from LNI
She has a question — why is her engine making a noise? She files a query through the app


Day 4 — Officer Molapo stops Litumeleng
She was speeding. Officer Molapo logs into the app on his device as officer1. He finds her vehicle by registration — he can see it's registered, who owns it, and whether the insurance is valid. He records a violation:
Vehicle: LSO-001-AA
Violation: Speeding
Fine: M500
Status: Unpaid
He can only see violations and reports he personally filed. He cannot see what Officer Thamae filed last week — those are not his cases.

Day 5 — LNI Insurance processes renewals
The insurance company logs in as insurer1. They see only their own policies — not Alliance Insurance policies, not Old Mutual policies. They see Litumeleng's policy is expiring in 25 days. They mark it for renewal. They add a new policy for another client.

Day 6 — Litumeleng pays her fine
She logs back in as customer1. On her My Insurance tab she can see her policy is expiring soon. On her queries tab, the admin has responded to her engine noise question. She calls the workshop to book a checkup.
Officer Molapo logs in. He marks Litumeleng's fine as Paid after she pays at the traffic department.

Day 7 — Admin reviews everything
The admin logs in and sees the full picture:

8 vehicles registered
5 customers
3 unpaid violations
4 active insurance policies
All activity across all modules

He can jump into any module, see everything, respond to queries, manage users, and generate reports.

## Why Each Module Exists
WORKSHOP MODULE
Real purpose: Workshops are authorized service centers.
They register new vehicles when someone buys a car.
They record every service so there's a permanent history.
A buyer of a second-hand car can see the full service history.

CUSTOMER MODULE  
Real purpose: Vehicle owners have a right to see their own data.
Litumeleng should know what services were done on her car.
She should know if her insurance is still valid.
She should be able to ask questions about her vehicle.

POLICE MODULE
Real purpose: Officers need to verify vehicles on the spot.
Is this vehicle registered? Who owns it?
Does it have valid insurance?
Has it been reported stolen?
Each officer only sees their own cases —
this prevents officers from tampering with each other's records.

INSURANCE MODULE
Real purpose: Insurance companies manage their own portfolio.
LNI can see all their policies and which ones are expiring.
They cannot see Alliance Insurance's clients —
that would be a privacy and business violation.

ADMIN MODULE
Real purpose: The government controls who has access.
Only the admin can create accounts.
Only the admin can see everything.
This is the "super user" — the system owner.

## How to Run
1. Clone the repository
2. Open in IntelliJ IDEA
3. Maven will auto-download dependencies
4. Run `MainApp.java`

## Database
Live on Neon PostgreSQL - connects automatically on launch.