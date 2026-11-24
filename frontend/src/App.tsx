import { lazy, Suspense } from 'react';
import { Outlet, Route, Routes } from 'react-router-dom';
import { Loading } from './components/ui';
import { Onboard } from './screens/tenant/Onboard';
import { Explore } from './screens/tenant/Explore';
import { Filters } from './screens/tenant/Filters';
import { MapView } from './screens/tenant/MapView';
import { Listing } from './screens/tenant/Listing';
import { Viewing } from './screens/tenant/Viewing';
import { Chat } from './screens/tenant/Chat';
import { Apply } from './screens/tenant/Apply';
import { Pay } from './screens/tenant/Pay';
import { MoveIn } from './screens/tenant/MoveIn';
import { MyHome } from './screens/tenant/MyHome';
import { NotFound } from './screens/NotFound';

// Screens outside the tenant's first visit load on demand, to keep the first
// download small on slow data.
const flow = () => import('./screens/landlord/ListingFlow');
const ListPin = lazy(() => flow().then((m) => ({ default: m.ListPin })));
const ListRoom = lazy(() => flow().then((m) => ({ default: m.ListRoom })));
const ListPhotos = lazy(() => flow().then((m) => ({ default: m.ListPhotos })));
const ListPublish = lazy(() => flow().then((m) => ({ default: m.ListPublish })));
const LandlordHome = lazy(() => import('./screens/landlord/LandlordHome').then((m) => ({ default: m.LandlordHome })));
const Requests = lazy(() => import('./screens/landlord/Requests').then((m) => ({ default: m.Requests })));
const RentBook = lazy(() => import('./screens/landlord/RentBook').then((m) => ({ default: m.RentBook })));
const Lease = lazy(() => import('./screens/landlord/Lease').then((m) => ({ default: m.Lease })));
const Checker = lazy(() => import('./screens/trust/Checker').then((m) => ({ default: m.Checker })));
const Report = lazy(() => import('./screens/trust/Report').then((m) => ({ default: m.Report })));
const Dispute = lazy(() => import('./screens/trust/Dispute').then((m) => ({ default: m.Dispute })));
const Admin = lazy(() => import('./screens/admin/Admin').then((m) => ({ default: m.Admin })));
const Saved = lazy(() => import('./screens/living/Saved').then((m) => ({ default: m.Saved })));
const Notifications = lazy(() => import('./screens/living/Notifications').then((m) => ({ default: m.Notifications })));
const Wanted = lazy(() => import('./screens/living/Wanted').then((m) => ({ default: m.Wanted })));
const Meter = lazy(() => import('./screens/living/Meter').then((m) => ({ default: m.Meter })));
const Repair = lazy(() => import('./screens/living/Repair').then((m) => ({ default: m.Repair })));
const MoveOut = lazy(() => import('./screens/living/MoveOut').then((m) => ({ default: m.MoveOut })));
const Reviews = lazy(() => import('./screens/living/Reviews').then((m) => ({ default: m.Reviews })));
const ScreenIndex = lazy(() => import('./screens/ScreenIndex').then((m) => ({ default: m.ScreenIndex })));

/** Phone width column for every screen except the back office. */
function PhoneFrame() {
  return (
    <div className="app-frame">
      <Suspense fallback={<Loading />}>
        <Outlet />
      </Suspense>
    </div>
  );
}

// Access control lives in the backend (role checks plus ownership checks in the
// services). These routes are not a security boundary; real sign in (Phase 2)
// decides what the API will serve to whom.
export function App() {
  return (
    <Routes>
      <Route element={<PhoneFrame />}>
        <Route path="/" element={<Onboard />} />
        <Route path="/screens" element={<ScreenIndex />} />

        <Route path="/explore" element={<Explore />} />
        <Route path="/filters" element={<Filters />} />
        <Route path="/map" element={<MapView />} />
        <Route path="/listings/:id" element={<Listing />} />
        <Route path="/listings/:id/viewing" element={<Viewing />} />
        <Route path="/chat" element={<Chat />} />
        <Route path="/apply/:id" element={<Apply />} />
        <Route path="/pay" element={<Pay />} />
        <Route path="/move-in" element={<MoveIn />} />
        <Route path="/home" element={<MyHome />} />
        <Route path="/home/repair" element={<Repair />} />
        <Route path="/home/electricity" element={<Meter />} />
        <Route path="/home/move-out" element={<MoveOut />} />
        <Route path="/home/reviews" element={<Reviews />} />
        <Route path="/saved" element={<Saved />} />
        <Route path="/notifications" element={<Notifications />} />
        <Route path="/wanted" element={<Wanted />} />
        <Route path="/lease" element={<Lease />} />

        <Route path="/list/where" element={<ListPin />} />
        <Route path="/list/rent" element={<ListRoom />} />
        <Route path="/list/photos" element={<ListPhotos />} />
        <Route path="/list/publish" element={<ListPublish />} />
        <Route path="/landlord" element={<LandlordHome />} />
        <Route path="/landlord/requests" element={<Requests />} />
        <Route path="/landlord/rent-book" element={<RentBook />} />

        <Route path="/checker" element={<Checker />} />
        <Route path="/report" element={<Report />} />
        <Route path="/disputes/:id" element={<Dispute />} />

        <Route path="*" element={<NotFound />} />
      </Route>
      <Route
        path="/admin"
        element={
          <Suspense fallback={<Loading />}>
            <Admin />
          </Suspense>
        }
      />
    </Routes>
  );
}
