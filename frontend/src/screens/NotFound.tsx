import { Link } from 'react-router-dom';
import { Screen } from '../components/ui';

export function NotFound() {
  return (
    <Screen>
      <div className="state">
        <h1 className="title">This page is not here</h1>
        <p>The link may be old. Start again from the rooms.</p>
        <Link to="/explore" className="btn btn--primary btn--md">
          Explore rooms
        </Link>
      </div>
    </Screen>
  );
}
