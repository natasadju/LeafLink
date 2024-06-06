
import { RouterProvider, createBrowserRouter } from "react-router-dom";
import { Dashboard, HomeLayout, Landing, Login, Logout, Register, Parks, AddPark } from "./pages";
import EventDetails from "./pages/EventDetails";
import { ToastContainer, toast } from 'react-toastify';
import Test from "./pages/Test.jsx";

const router = createBrowserRouter([
  {
    path: "/",
    element: <HomeLayout />,
    children: [
      {
        index: true,
        element: <Landing />,
      },
      {
        path: "login",
        element: <Login />,
      },
      {
        path: "register",
        element: <Register />,
      },
      {
        path: "dashboard",
        element: <Dashboard />,
      },
      {
        path: "logout",
        element: <Logout />,
      },
      {
        path: "test",
        element: <Test />
      },
      {
        path: "parks",
        element: <Parks />,
      },
      {
        path: "addpark",
        element: <AddPark />,
      },
      {
        path: "events/:eventId",
        element: <EventDetails />,
      }
    ],
  },
]);

function App() {


  return (
    <>
        <RouterProvider router={router} />
        <ToastContainer position='top-center' />
    </>
  )
}

export default App